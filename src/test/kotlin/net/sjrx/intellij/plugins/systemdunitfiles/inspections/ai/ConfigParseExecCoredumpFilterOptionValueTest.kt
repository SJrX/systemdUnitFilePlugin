package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseExecCoredumpFilterOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      CoredumpFilter=default
      CoredumpFilter=all
      CoredumpFilter=default private-dax shared-dax
      CoredumpFilter=private-anonymous shared-anonymous private-file-backed shared-file-backed elf-headers private-huge shared-huge
      CoredumpFilter=private-file-backed private-dax 0xF
      CoredumpFilter=11
      CoredumpFilter=0x1101
      CoredumpFilter=0
      CoredumpFilter=ff deadbeef FFFFFFFFFFFFFFFF
      CoredumpFilter=-0
      CoredumpFilter=-0x00 +1f
      CoredumpFilter=
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
      CoredumpFilter=defaulta
      CoredumpFilter=default defaulta default
      CoredumpFilter=Default
      CoredumpFilter=private
      CoredumpFilter=0x
      CoredumpFilter=1FFFFFFFFFFFFFFFF
      CoredumpFilter=12g
      CoredumpFilter=-1
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(8, highlights)
  }
}
