package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseHtbClassU32OptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // sd-check at 0f0682b773 accepts these: strtoul starts afresh after the 0b/0o prefix, so a sign or blank may follow it.
    // language="unit file (systemd)"
    val file = """
      [HierarchyTokenBucketClass]
      Priority=0
      Priority=1
      Priority=20480
      Priority=4294967295
      Priority=0x10
      Priority=0o17
      Priority=0b101
      Priority=017
      Priority=+5
      Priority=-0
      Priority=0b+1
      Priority=0b-0
      Priority=0b 1
      Priority=0o 7
      Priority=0b +1
      Priority=0b -0
      Priority=
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
      [HierarchyTokenBucketClass]
      Priority=4294967296
      Priority=-1
      Priority=08
      Priority=0x
      Priority=1.5
      Priority=abc
      Priority=+0b1
      Priority=12 13
      Priority=0b-1
      Priority=0b -1
      Priority=-0b0
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(11, highlights)
  }

}
