package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.*

class SemanticDataRepositoryTest : AbstractUnitFileTest() {
  fun testInteresting() {
    val sdr = SemanticDataRepository.instance
    assertInstanceOf(sdr.getOptionValidator(FileClass.UNIT_FILE,"Socket", "SendSIGKILL"), BooleanOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator(FileClass.UNIT_FILE,"Unit", "Documentation"), DocumentationOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator(FileClass.UNIT_FILE, "Service", "KillMode"), KillModeOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator(FileClass.UNIT_FILE,"Mount", "KillMode"), KillModeOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator(FileClass.UNIT_FILE, "Socket", "DirectoryMode"), ModeStringOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator(FileClass.UNIT_FILE , "Unit", "XXXX"), NullOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator(FileClass.UNIT_FILE, "XXXX", "Yes"), NullOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator(FileClass.UNIT_FILE, "Service", "Restart"), RestartOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator(FileClass.UNIT_FILE, "Service", "Type"), ServiceTypeOptionValue::class.java)
  }

  fun testDeclaredUnderKeywordDiffers() {
    val sdr = SemanticDataRepository.instance

    for (fileClass in FileClass.entries) {
      for (section in sdr.getSectionNamesForFile(fileClass.name)) {
        val data = sdr.getKeyValuePairsForSectionFromDocumentation(fileClass, section)
        for ((key, value) in data) {
          val declaredUnderKeyword = value.declaredUnderKeyword
          if (declaredUnderKeyword != null && declaredUnderKeyword != key) {
            println("Mismatch: $section.$key: $declaredUnderKeyword")
          }
        }
      }
    }

  }

  fun testAllRequiredOptionsExists () {

    // Fixture Setup

    val sdr = SemanticDataRepository.instance

    val files = listOf(
      "file.automount",
      "file.device",
      "file.mount",
      "file.path",
      "file.service",
      "file.slice",
      "file.socket",
      "file.swap",
      "file.target",
      "file.timer",
    )


    // Execute SUT & Verification


    for (file in files) {
      val keys = sdr.getRequiredKeys(file)

      for (key in keys) {
        val sectionAndKey = key.split('.')
        val validKeys = sdr.getAllowedKeywordsInSectionFromValidators(FileClass.UNIT_FILE, sectionAndKey[0])

        assertContainsElements(validKeys, sectionAndKey[1])
      }
    }
  }
}
