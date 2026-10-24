package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** config_parse_match_strv: glob lists with only quote balance checked. */
class ConfigParseMatchStrvOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValuesNetwork() {
    // language="unit file (systemd)"
    val file = """
            [Match]
            Path=pci-0000:02:00.0-*
            Driver=dummy
            Driver=!e1000e igb
            Type=ether wlan
            Type=!loopback
            Kind=dummy veth
            Kind=!
            WLANInterfaceType=ap station
            SSID="My Home Wifi" 'Other Net'
            SSID=Cafe\ Wifi
            SSID=pre"fix mid"dle
            Path=
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidValuesNetwork() {
    // One highlight per line.
    // language="unit file (systemd)"
    val file = """
            [Match]
            SSID="unterminated
            Kind=bond 'veth
            Driver=a"b
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(3, myFixture.doHighlighting())
  }
  @Test
  fun testValidValuesLink() {
    // language="unit file (systemd)"
    val file = """
            [Match]
            Path=pci-0000:00:1f.6
            Driver=e1000e
            Type=ether
            Kind=!*
        """.trimIndent()

    setupFileInEditor("file.link", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidValuesLink() {
    // One highlight per line.
    // language="unit file (systemd)"
    val file = """
            [Match]
            Path="pci-0000
        """.trimIndent()

    setupFileInEditor("file.link", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(1, myFixture.doHighlighting())
  }
}
