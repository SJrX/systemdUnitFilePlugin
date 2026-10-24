package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseHtbU32OptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // language="unit file (systemd)"
    val file = """
      [HierarchyTokenBucket]
      RateToQuantum=0
      RateToQuantum=1
      RateToQuantum=20480
      RateToQuantum=4294967295
      RateToQuantum=0x10
      RateToQuantum=0o17
      RateToQuantum=0b101
      RateToQuantum=017
      RateToQuantum=+5
      RateToQuantum=-0
      RateToQuantum=
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
      [HierarchyTokenBucket]
      RateToQuantum=4294967296
      RateToQuantum=-1
      RateToQuantum=08
      RateToQuantum=0x
      RateToQuantum=1.5
      RateToQuantum=abc
      RateToQuantum=+0b1
      RateToQuantum=12 13
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(8, highlights)
  }

}
