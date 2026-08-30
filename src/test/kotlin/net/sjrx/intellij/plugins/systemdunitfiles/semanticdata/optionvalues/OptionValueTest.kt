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

    val startDate = LocalDate.of(2025, 10, 27) // Today's date
    val startingCount = 619 // Your current undocumented options count
    val currentDate = LocalDate.now()
    val daysSinceStart = ChronoUnit.DAYS.between(startDate, currentDate)
    val reductionPerDay = 1
    val allowed = maxOf(0, startingCount - (daysSinceStart * reductionPerDay))

    // The allowance shrinks by reductionPerDay each day; the test fails once it reaches the current
    // missing count. Solving allowed <= missing for the date gives the day this count stops passing.
    val failDate = startDate.plusDays(((startingCount - missingValidators.size) / reductionPerDay).toLong())

    println("Missing Keywords:$totalMissingValidators")
    println("Missing Validators:${missingValidators.size}")
    println("Allowed Missing Validation: $allowed")
    println("Found:$totalFoundValidators")
    if (missingValidators.size >= allowed) {
      println("Burndown: test started failing on $failDate (at the current missing count)")
    } else {
      println("Burndown: test will start to fail on $failDate unless the count keeps dropping")
    }

    if (missingValidators.size >= allowed) {
      assertEquals("Number of missing validators is too high at ${missingValidators.size} > $allowed vs. found ${foundValidators.size} ${totalFoundValidators}", sortedList, "")
    } else {
        println("Missing Validators:")
        println(sortedList)
    }

    if (totalFoundValidators == 0) {
      fail("There are no found validators, something is wrong")
    }

  }
}
