package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.Strings
import com.intellij.psi.search.FilenameIndex
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
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
    val autoCompletionOptions: MutableSet<String> = HashSet(unitNames)
    for (unitTypes in validUnitTypes) {
      for (file in FilenameIndex.getAllFilesByExt(project, unitTypes)) {
        autoCompletionOptions.add(file.name)
      }
    }
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
