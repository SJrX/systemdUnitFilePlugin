package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseConcurrencyMaxOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [Slice]
      ConcurrencySoftMax=infinity
      ConcurrencyHardMax=10
      ActivatingConcurrencyMax=0
      ConcurrencySoftMax=4294967295
      ConcurrencyHardMax=0x10
      ActivatingConcurrencyMax=010
      ConcurrencySoftMax=0b101
      ConcurrencyHardMax=0o17
      ActivatingConcurrencyMax=+5
      ConcurrencySoftMax=
    """.trimIndent()

    setupFileInEditor("file.slice", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [Slice]
      ConcurrencySoftMax=4294967296
      ConcurrencyHardMax=-1
      ActivatingConcurrencyMax=Infinity
      ConcurrencySoftMax=unlimited
      ConcurrencyHardMax=08
      ActivatingConcurrencyMax=10 20
      ConcurrencySoftMax=1.5
    """.trimIndent()

    setupFileInEditor("file.slice", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(7, highlights)
  }
}
