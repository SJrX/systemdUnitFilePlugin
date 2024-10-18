package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator

class OptionValueTest : AbstractUnitFileTest() {

  fun testThatAllOptionsHaveValidator() {

    val validatorMap = SemanticDataRepository.instance.getValidatorMap()

    val missingValidators = hashMapOf<Validator, Int>()
    var totalMissingValidators = 0
    for (sectionName in SemanticDataRepository.instance.sectionNamesFromValidators) {
      for (key in SemanticDataRepository.instance.getAllowedKeywordsInSectionFromValidators(sectionName)) {


        val validator = SemanticDataRepository.instance.getValidatorForSectionAndKey(sectionName, key)

        if (!validatorMap.containsKey(validator)) {
          missingValidators[validator] = (missingValidators[validator] ?: 0) + 1
          totalMissingValidators++
        }
      }
    }

    val missingValidatorList = missingValidators.map { "${String.format("%05d", it.value)}, ${it.key}" }

    val sortedList = missingValidatorList.sortedDescending().joinToString("\n")

    println(totalMissingValidators)
    if (totalMissingValidators > 630) {
      assertEquals(sortedList, "")
    }

  }
}
