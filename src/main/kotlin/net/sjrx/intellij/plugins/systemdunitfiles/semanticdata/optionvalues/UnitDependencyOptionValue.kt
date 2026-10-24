package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.Strings
import com.intellij.openapi.vfs.newvfs.persistent.FSRecords
import com.intellij.psi.search.FilenameIndex.processAllFileNames
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.containers.CollectionFactory
import com.intellij.psi.util.PsiTreeUtil
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionGroups
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * This validator is used for the config_parse_unit_deps validator.
 */
class UnitDependencyOptionValue : OptionValueInformation {
  /**
   * Default Constructor.
   */
  init {
    if (unitNames == null) {
      val filename: String = SemanticDataRepository.SEMANTIC_DATA_ROOT + "ubuntu-units.txt"
      val unitListing = this.javaClass.classLoader.getResource(filename)
      try {

        val reader = (unitListing?.openStream()?:InputStream.nullInputStream()).bufferedReader()
        val unitNames: SortedSet<String> = TreeSet()

        reader.forEachLine {
          unitNames.add(it)
        }

        Companion.unitNames = Collections.unmodifiableSet(unitNames)
      } catch (e: NullPointerException) {
        throw IllegalStateException("Could not read file: $filename", e)
      } catch (e: IOException) {
        throw IllegalStateException("Could not read file: $filename", e)
      }
    }
  }

  override fun getAutoCompleteOptions(project: Project): Set<String> {
    val autoCompletionOptions = CollectionFactory.createSmallMemoryFootprintSet(unitNames ?: emptySet())
    autoCompletionOptions.addAll(getAllProjectUnitFileNames(project))
    return autoCompletionOptions
  }

  override fun getErrorMessage(value: String): String? {
    throw IllegalStateException("Not supported")
  }

  /**
   * Checks every whitespace-separated word the way systemd does.
   *
   * For [Unit] dependencies,
   * [config_parse_unit_deps](https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L261-L316)
   * runs [unit_name_printf](https://github.com/systemd/systemd/blob/0f0682b773/src/core/unit-printf.c#L147-L177) on
   * each word and then adds the dependency, which requires
   * [unit_name_is_valid](https://github.com/systemd/systemd/blob/0f0682b773/src/basic/unit-name.c#L36-L82). A
   * template such as `foo@.service` is fine: it gets this unit's instance filled in by
   * [resolve_template](https://github.com/systemd/systemd/blob/0f0682b773/src/core/unit.c#L3320-L3350).
   *
   * The [Install] keys are read by systemctl, not PID 1. Also= is checked with unit_name_is_valid while the file is
   * parsed ([install_info_add](https://github.com/systemd/systemd/blob/0f0682b773/src/shared/install.c#L1194-L1218)),
   * and WantedBy=/RequiredBy= when the unit is enabled
   * ([install_info_symlink_wants](https://github.com/systemd/systemd/blob/0f0682b773/src/shared/install.c#L2090-L2117)).
   * Both expand the same specifiers through
   * [install_name_printf](https://github.com/systemd/systemd/blob/0f0682b773/src/shared/install-printf.c#L89-L114).
   *
   * PID 1 also refuses an instance or template of a type that cannot be templated
   * ([unit_add_name](https://github.com/systemd/systemd/blob/0f0682b773/src/core/unit.c#L282-L283),
   * [unit_type_may_template](https://github.com/systemd/systemd/blob/0f0682b773/src/shared/unit-file.h#L35-L42)), so
   * `After=foo@bar.mount` is dropped. systemctl does not check that for the [Install] keys, so neither does this.
   *
   * A word that uses a specifier (`foo@%i.service`, `notify@%n`) is accepted as long as every specifier is known,
   * because what it expands to depends on the unit's own name. An unknown one (`%f`, `%T`, `%Z`) makes
   * [specifier_printf](https://github.com/systemd/systemd/blob/0f0682b773/src/shared/specifier.c#L78-L80) fail
   * with EBADSLT, and the word is dropped.
   */
  override fun generateProblemDescriptors(property: UnitFilePropertyType, holder: ProblemsHolder) {
    // A line continuation separates words just like whitespace does. Blank it out with the same number of
    // characters, so that offsets still line up with the raw value text.
    val values = LINE_CONTINUATION.replace(property.valueNode.text) { " ".repeat(it.value.length) }
    val inInstallSection = PsiTreeUtil.getParentOfType(property, UnitFileSectionGroups::class.java)?.sectionName == "Install"

    for (match in WORD.findAll(values)) {
      val word = match.value
      val wordStart = match.range.first
      val problem = problemForWord(word, checkTemplateType = !inInstallSection) ?: continue
      holder.registerProblem(
        property.valueNode.psi,
        TextRange(wordStart + problem.first.first, wordStart + problem.first.last + 1),
        problem.second
      )
    }
  }

  /** The range within [word] to highlight and the message, or null if systemd accepts the word. */
  private fun problemForWord(word: String, checkTemplateType: Boolean): Pair<IntRange, String>? {
    var hasSpecifier = false
    var i = 0
    while (i < word.length) {
      if (word[i] == '%' && i + 1 < word.length) {
        val next = word[i + 1]
        when {
          next == '%' -> i += 2
          next in UNIT_NAME_PRINTF_SPECIFIERS -> {
            hasSpecifier = true
            i += 2
          }
          next in 'a'..'z' || next in 'A'..'Z' || next in '0'..'9' ->
            return Pair(i..i + 1, "Unknown specifier %$next, systemd ignores this unit name")
          else -> i += 1
        }
      } else {
        i += 1
      }
    }

    if (hasSpecifier) {
      // What the specifiers expand to is only known when the unit is loaded.
      return null
    }

    if (word.length >= UNIT_NAME_MAX) {
      return Pair(word.indices, "Unit name is too long, it must be shorter than $UNIT_NAME_MAX characters")
    }

    val lastDot = word.lastIndexOf('.')
    if (lastDot == -1) {
      return Pair(word.indices, "Invalid unit name detected, all units must end with a . and then a unit type suffix")
    }

    val unitType = word.substring(lastDot + 1)
    if (!validUnitTypes.contains(unitType)) {
      return Pair(lastDot + 1 until word.length, "Unit type " + unitType + " is unsupported, valid types are: " + Strings.join(validUnitTypes, ", "))
    }

    if (lastDot == 0) {
      return Pair(word.indices, "Invalid unit name detected, the name in front of the unit type suffix must not be empty")
    }

    for (index in 0 until lastDot) {
      if (word[index] !in VALID_UNIT_NAME_CHARS) {
        return Pair(index..index, "Invalid character '${word[index]}' in unit name, allowed are ASCII letters, digits, ':', '-', '_', '.', '\\' and '@'")
      }
    }

    if (word[0] == '@') {
      return Pair(0..0, "Invalid unit name detected, a unit name must not start with '@'")
    }

    if (checkTemplateType && word.indexOf('@') in 0 until lastDot && unitType !in TEMPLATABLE_UNIT_TYPES) {
      return Pair(word.indices, "Units of type $unitType cannot be templates or instances, only these can: " + Strings.join(TEMPLATABLE_UNIT_TYPES, ", "))
    }

    return null
  }

  override val validatorName: String
    get() = VALIDATOR_NAME

  /**
   * That's a simplified version of `com.intellij.psi.search.FilenameIndex.getAllFilesByExt()`
   * since we need only file names, but not whole VirtualFile's
   */
  private fun getAllProjectUnitFileNames(project: Project): Set<String> {
    return CachedValuesManager.getManager(project).getCachedValue(project, CachedValueProvider {
      return@CachedValueProvider CachedValueProvider.Result<Set<String>>(
              getAllProjectUnitFileNamesImpl(project),
              getFilenameIndexModificationTracker(),
      )
    })
  }

  /*
   * Simplified copy of com.intellij.openapi.fileEditor.impl.UniqueVFilePathBuilderImpl.getFilenameIndexModificationTracker
   * Assuming FileBasedIndexExtension.USE_VFS_FOR_FILENAME_INDEX == true (it was set so at least since 2022.2)
   */
  private fun getFilenameIndexModificationTracker(): ModificationTracker {
    return ModificationTracker {
      @Suppress("UnstableApiUsage")
      FSRecords.getNamesIndexModCount()
    }
  }

  private fun getAllProjectUnitFileNamesImpl(project: Project): Set<String> {
    val names = CollectionFactory.createSmallMemoryFootprintSet<String>()
    val minNameLength = validUnitTypes.minOf { it.length } + 2 /* dot and at least on character in name */
    val maxExtLength = validUnitTypes.maxOf { it.length }

    processAllFileNames({ name: String ->
      val length = name.length
      if (length >= minNameLength) {
        val dot = name.lastIndexOf('.')
        if (dot != -1 && (length - (dot + 1) <= maxExtLength)) {
          val ext = name.substring(dot + 1)
          if (ext in validUnitTypes || ext.lowercase() in validUnitTypes) {
            names.add(name)
          }
        }
      }
      true
    }, GlobalSearchScope.allScope(project), null)
    return names
  }

  companion object {
    @Volatile
    private var unitNames: Set<String>? = null
    private val validUnitTypes = setOf("service", "socket", "device", "mount", "automount", "swap", "target", "path", "timer", "slice", "scope")

    /** unit_type_may_template() in src/shared/unit-file.h. */
    private val TEMPLATABLE_UNIT_TYPES = setOf("service", "socket", "target", "timer", "path")

    /** UNIT_NAME_MAX: a unit name must be shorter than this. */
    private const val UNIT_NAME_MAX = 256

    /** VALID_CHARS_WITH_AT in src/basic/unit-name.c. */
    private val VALID_UNIT_NAME_CHARS = (('a'..'z') + ('A'..'Z') + ('0'..'9') + listOf(':', '-', '_', '.', '\\', '@')).toSet()

    /** The specifiers unit_name_printf() and install_name_printf() know: their own five plus COMMON_SYSTEM and COMMON_CREDS. */
    private const val UNIT_NAME_PRINTF_SPECIFIERS = "ijnNpaAbBHlqmMovwWgGuU"

    private val LINE_CONTINUATION = Regex("\\\\[ \\t]*\\r?\\n")

    private val WORD = Regex("\\S+")

    const val VALIDATOR_NAME = "config_parse_unit_deps"
    val validators = mapOf(
      Validator(VALIDATOR_NAME, "*") to UnitDependencyOptionValue(),
    )
  }
}
