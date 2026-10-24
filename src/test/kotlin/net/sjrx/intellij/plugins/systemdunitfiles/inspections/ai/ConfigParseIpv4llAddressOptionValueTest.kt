package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseIpv4llAddressOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      IPv4LLStartAddress=169.254.133.11
      IPv4LLStartAddress=169.254.1.0
      IPv4LLStartAddress=169.254.254.255
      IPv4LLStartAddress=
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
      [Network]
      IPv4LLStartAddress=169.254.0.5
      IPv4LLStartAddress=169.254.255.1
      IPv4LLStartAddress=192.168.1.1
      IPv4LLStartAddress=169.254.1.256
      IPv4LLStartAddress=fe80::1
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(5, highlights)
  }
}
