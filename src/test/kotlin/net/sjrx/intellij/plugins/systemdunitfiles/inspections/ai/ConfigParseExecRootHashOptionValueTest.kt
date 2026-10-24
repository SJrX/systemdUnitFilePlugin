package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseExecRootHashOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      RootHash=e0e5e0b9e6d9bd4b8d2e2e3cbb6f0f4d0b0c0e5e0b9e6d9bd4b8d2e2e3cbb6f0
      RootHash=0123456789ABCDEF0123456789abcdef
      RootHash=01234567 89abcdef 01234567 89abcdef
      RootHash=/var/lib/images/foo.roothash
      RootHash=/anything goes here
      RootHash=
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      RootHash=0123456789abcdef
      RootHash=0123456789abcdef0123456789abcdef0
      RootHash=0123456789abcdefg123456789abcdef
      RootHash=%S/foo.roothash
      RootHash=foo.roothash
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(5, highlights)
  }
}
