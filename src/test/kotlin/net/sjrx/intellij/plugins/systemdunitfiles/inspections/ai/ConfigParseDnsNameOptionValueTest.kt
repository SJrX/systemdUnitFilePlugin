package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDnsNameOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidDnsNames() {
    // language="unit file (systemd)"
    val file = """
      [DHCPServer]
      BootServerName=valid-hostname.example.com
      BootServerName=valid.hostname
      BootServerName=single
      BootServerName=name_with_underscore
      BootServerName=mixed_chars-123.example.com
      BootServerName=trailing.dot.
      BootServerName=-leading.hyphen
      BootServerName=trailing-.com
      BootServerName=has!bang
      Domain=example.org
      LocalLeaseDomain=lan.local
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidDnsNames() {
    // language="unit file (systemd)"
    val file = """
      [DHCPServer]
      BootServerName=double..dot
      Domain=.leading.dot
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(2, highlights)
  }
}
