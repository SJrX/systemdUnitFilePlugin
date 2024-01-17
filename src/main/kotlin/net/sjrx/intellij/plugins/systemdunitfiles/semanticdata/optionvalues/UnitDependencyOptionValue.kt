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
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
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

  override fun generateProblemDescriptors(property: UnitFilePropertyType, holder: ProblemsHolder) {
    // Valid unit names consist of a "name prefix" and a dot and a suffix specifying the unit type.
    // The "unit prefix" must consist of one or more valid characters (ASCII letters, digits, ":", "-", "_", ".", and "\").
    // The total length of the unit name including the suffix must not exceed 256 characters.
    // The type suffix must be one of ".service", ".socket", ".device", ".mount", ".automount", ".swap", ".target", ".path", ".timer", ".slice", or ".scope".
    val values = property.valueNode.text
    var processedCharactersInString = 0
    while (processedCharactersInString < values.length) {
      val indexOfNextSpace = values.indexOf(" ", processedCharactersInString)
      var endOfWord = values.length
      if (indexOfNextSpace > -1) {
        endOfWord = indexOfNextSpace
      }
      val word = values.substring(processedCharactersInString, endOfWord)
      if (word.length == 0) {
        processedCharactersInString += 1
        continue
      }
      val lastDot = word.lastIndexOf('.')
      val range = TextRange(processedCharactersInString + lastDot + 1, endOfWord)
      if (lastDot > -1) {
        // Valid unit name
        val unitType = word.substring(lastDot + 1)
        if (!validUnitTypes.contains(unitType)) {
          holder.registerProblem(
                  property.valueNode.psi, range,
                  "Unit type " + unitType + " is unsupported, valid types are: " + Strings.join(validUnitTypes, ", ")
          )
        }
      } else {
        holder.registerProblem(property.valueNode.psi, range, "Invalid unit name detected, all units must end with a . and then a unit type suffix")
      }
      processedCharactersInString += word.length
    }
    return
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

    const val VALIDATOR_NAME = "config_parse_unit_deps"
    val validators = mapOf(
      Validator(VALIDATOR_NAME, "*") to UnitDependencyOptionValue(),
    )
  }
}
