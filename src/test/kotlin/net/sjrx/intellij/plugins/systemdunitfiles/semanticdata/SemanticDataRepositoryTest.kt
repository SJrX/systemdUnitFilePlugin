package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.*

class SemanticDataRepositoryTest : AbstractUnitFileTest() {
  fun testInteresting() {
    val sdr = SemanticDataRepository.instance
    assertInstanceOf(sdr.getOptionValidator("Socket", "SendSIGKILL"), BooleanOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator("Unit", "Documentation"), DocumentationOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator("Service", "KillMode"), KillModeOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator("Mount", "KillMode"), KillModeOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator("Socket", "DirectoryMode"), ModeStringOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator("Unit", "XXXX"), NullOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator("XXXX", "Yes"), NullOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator("Service", "Restart"), RestartOptionValue::class.java)
    assertInstanceOf(sdr.getOptionValidator("Service", "Type"), ServiceTypeOptionValue::class.java)
  }

  fun testDeclaredUnderKeywordDiffers() {
    val sdr = SemanticDataRepository.instance
    for (section in sdr.sectionNamesFromDocumentation) {
      val data = sdr.getKeyValuePairsForSectionFromDocumentation(section)
      for ((key, value) in data) {
        val declaredUnderKeyword = value.declaredUnderKeyword
        if (declaredUnderKeyword != null && declaredUnderKeyword != key) {
          println("Mismatch: $section.$key: $declaredUnderKeyword")
        }
      }
    }
  }
}
