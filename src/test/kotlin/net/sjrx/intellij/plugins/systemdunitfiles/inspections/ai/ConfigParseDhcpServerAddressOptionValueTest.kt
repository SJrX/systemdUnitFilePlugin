package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDhcpServerAddressOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [DHCPServer]
      ServerAddress=192.168.5.1/24
      ServerAddress=0.0.0.0/24
      ServerAddress=10.0.0.1
      ServerAddress=10.0.0.1/0
      ServerAddress=10.0.0.1/32
      ServerAddress=10.0.0.1/0x18
      ServerAddress=10.0.0.1/ 24
      ServerAddress=169.253.255.255/16
      ServerAddress=128.0.0.1
      ServerAddress=
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
      [DHCPServer]
      ServerAddress=192.168.5.1/33
      ServerAddress=2001:db8::1/64
      ServerAddress=192.168.5
      ServerAddress=server.example.com
      ServerAddress=127.0.0.1/8
      ServerAddress=127.255.0.1
      ServerAddress=169.254.1.1/16
      ServerAddress=169.254.0.0/16
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(8, highlights)
  }
}
