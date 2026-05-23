package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.FileClass
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class OptionValueTest : AbstractUnitFileTest() {

  fun testThatAllOptionsHaveValidator() {

    val validatorMap = SemanticDataRepository.instance.getValidatorMap()

    val missingValidators = hashMapOf<Validator, Int>()
    var totalMissingValidators = 0
    var totalFoundValidators = 0
    val foundValidators = mutableSetOf<Validator>()
    for (fileClass in FileClass.entries) {
      for (sectionName in SemanticDataRepository.instance.getSectionNamesForFile(fileClass.fileClass)) {
        for (key in SemanticDataRepository.instance.getAllowedKeywordsInSectionFromValidators(fileClass, sectionName)) {

          val validator = SemanticDataRepository.instance.getValidatorForSectionAndKey(fileClass, sectionName, key)

          if (!validatorMap.containsKey(validator)) {
            missingValidators[validator] = (missingValidators[validator] ?: 0) + 1
            totalMissingValidators++
          } else {
            foundValidators.add(validator)
            totalFoundValidators++
          }
        }
      }
    }
    val missingValidatorList = missingValidators.map { "${String.format("%05d", it.value)}, ${it.key}" }

    val sortedList = missingValidatorList.sortedDescending().joinToString("\n")

    println("Missing:$totalMissingValidators")
    println("Missing Functions:${missingValidators.size}")
    println("Found:$totalFoundValidators")

    val startDate = LocalDate.of(2025, 10, 27) // Today's date
    val startingCount = 619 // Your current undocumented options count
    val currentDate = LocalDate.now()
    val daysSinceStart = ChronoUnit.DAYS.between(startDate, currentDate)
    val reductionPerDay = 1
    val allowed = maxOf(0, startingCount - (daysSinceStart * reductionPerDay))

    if (missingValidators.size >= allowed) {
      assertEquals("Number of missing functions is too high at ${missingValidators.size} > $allowed vs. found ${foundValidators.size} ${totalFoundValidators}", sortedList, "")
    }

    if (totalFoundValidators == 0) {
      fail("There are no found validators, something is wrong")
    }

  }
}
