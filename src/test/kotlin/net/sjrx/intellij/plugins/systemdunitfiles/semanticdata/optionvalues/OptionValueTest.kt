package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.FileClass
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator

class OptionValueTest : AbstractUnitFileTest() {

  fun testThatAllOptionsHaveValidator() {

    val validatorMap = SemanticDataRepository.instance.getValidatorMap()

    val missingValidators = hashMapOf<Validator, Int>()
    var totalMissingValidators = 0
    var totalFoundValidators = 0
    for (fileClass in FileClass.entries) {
      for (sectionName in SemanticDataRepository.instance.getSectionNamesForFile(fileClass.fileClass)) {
        for (key in SemanticDataRepository.instance.getAllowedKeywordsInSectionFromValidators(fileClass, sectionName)) {

          val validator = SemanticDataRepository.instance.getValidatorForSectionAndKey(fileClass, sectionName, key)

          if (!validatorMap.containsKey(validator)) {
            missingValidators[validator] = (missingValidators[validator] ?: 0) + 1
            totalMissingValidators++
          } else {
            totalFoundValidators++
          }
        }
      }
    }
    val missingValidatorList = missingValidators.map { "${String.format("%05d", it.value)}, ${it.key}" }

    val sortedList = missingValidatorList.sortedDescending().joinToString("\n")

    println("Missing:$totalMissingValidators")
    println("Found:$totalFoundValidators")
    if (totalMissingValidators > 530) {
      assertEquals("Number of missing validators is too high at ${totalMissingValidators} vs. found ${totalFoundValidators}", sortedList, "")
    }

    if (totalFoundValidators == 0) {
      fail("There are no found validators, something is wrong")
    }

  }
}
