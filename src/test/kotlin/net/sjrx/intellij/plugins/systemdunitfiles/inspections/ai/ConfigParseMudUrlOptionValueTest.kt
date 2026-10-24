package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseMudUrlOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [DHCPv4]
      MUDURL=https://www.example.com/mud.json
      MUDURL=http://example.com/a b
      MUDURL=https://example.com/\x41
      MUDURL=
      [DHCPv6]
      MUDURL=https://example.com/
      [LLDP]
      MUDURL=http://x
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
      [DHCPv4]
      MUDURL=ftp://example.com/mud.json
      MUDURL=https://
      MUDURL=HTTPS://example.com/
      [DHCPv6]
      MUDURL=https://example.com/café
      [LLDP]
      MUDURL=https://example.com/\q
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(5, highlights)
  }
}
