package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseConcurrencyMaxOptionValueTest : AbstractUnitFileTest() {

  // Every value below was checked with sd-check (systemd 0f0682b773). The 0b/0o forms with a sign or a space after
  // the prefix are accepted because strtoul() starts afresh after mangle_base(); "0b -1" and "0b-1" are out of
  // range (-ERANGE), "+0b1", "-0b0", "+0o7", "08" and "0x" are -EINVAL. (systemd also accepts a leading \v, but the
  // unit-file lexer does not let one into a value, so SafeAtoTerminalTest covers that.)
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
      ConcurrencyHardMax=-0
      ActivatingConcurrencyMax=-0x0
      ConcurrencySoftMax=+0x10
      ConcurrencyHardMax=0b+1
      ActivatingConcurrencyMax=0b-0
      ConcurrencySoftMax=0b 1
      ConcurrencyHardMax=0o 7
      ActivatingConcurrencyMax=0b +1
      ConcurrencySoftMax=0b -0
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
      ConcurrencyHardMax=+0b1
      ActivatingConcurrencyMax=+0o7
      ConcurrencySoftMax=-0b0
      ConcurrencyHardMax=0b-1
      ActivatingConcurrencyMax=0b -1
      ConcurrencySoftMax=0x
    """.trimIndent()

    setupFileInEditor("file.slice", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(13, highlights)
  }
}
