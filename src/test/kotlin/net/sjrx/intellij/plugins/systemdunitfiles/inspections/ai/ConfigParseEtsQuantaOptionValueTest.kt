package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseEtsQuantaOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // language="unit file (systemd)"
    val file = """
      [EnhancedTransmissionSelection]
      QuantumBytes=2 4 6
      QuantumBytes=1001 1002 1003
      QuantumBytes=1004 1005
      QuantumBytes=1K
      QuantumBytes=1K512B
      QuantumBytes=1.5K 2M
      QuantumBytes=4294967295
      QuantumBytes=1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16
      QuantumBytes=
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [EnhancedTransmissionSelection]
      QuantumBytes=0
      QuantumBytes=4G
      QuantumBytes=1 0 2
      QuantumBytes=1K 512 B
      QuantumBytes=abc
      QuantumBytes=1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17
      QuantumBytes=1k
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(7, highlights)
  }

}
