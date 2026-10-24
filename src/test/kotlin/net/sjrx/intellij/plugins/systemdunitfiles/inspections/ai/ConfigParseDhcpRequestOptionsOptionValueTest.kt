package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDhcpRequestOptionsOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [DHCPv4]
      RequestOptions=59 60
      RequestOptions=17
      RequestOptions=1 254 0xfe 012
      RequestOptions=0b+1 0b1
      RequestOptions=
      [DHCPv6]
      RequestOptions=23 24
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
      RequestOptions=0
      RequestOptions=255
      RequestOptions=59,60
      RequestOptions=0b 1
      RequestOptions=0o 7
      [DHCPv6]
      RequestOptions=dns
      RequestOptions=0b-0
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(7, highlights)
  }
}
