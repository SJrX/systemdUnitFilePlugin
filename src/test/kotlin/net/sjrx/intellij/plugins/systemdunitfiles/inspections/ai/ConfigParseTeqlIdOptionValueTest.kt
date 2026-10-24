package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseTeqlIdOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // sd-check at 0f0682b773 accepts these: strtoul starts afresh after the 0b/0o prefix, so a sign or blank may follow it.
    // language="unit file (systemd)"
    val file = """
      [TrivialLinkEqualizer]
      Id=0
      Id=1
      Id=2147483647
      Id=0x7fffffff
      Id=0o7
      Id=0b+1
      Id=0b-0
      Id=0b 1
      Id=0o 7
      Id=0b +1
      Id=0b -0
      Id=
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // sd-check at 0f0682b773 rejects these: 0b-1 and 0b -1 are -1 (ERANGE), -0b0 is -0 followed by junk (EINVAL).
    // language="unit file (systemd)"
    val file = """
      [TrivialLinkEqualizer]
      Id=2147483648
      Id=4294967295
      Id=-1
      Id=abc
      Id=teql1
      Id=0b-1
      Id=0b -1
      Id=-0b0
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(8, highlights)
  }

}
