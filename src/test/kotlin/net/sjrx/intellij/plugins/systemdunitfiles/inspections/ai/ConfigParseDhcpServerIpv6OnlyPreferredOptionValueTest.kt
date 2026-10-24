package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDhcpServerIpv6OnlyPreferredOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // systemd refuses values under 300s outside its test mode; that magnitude check isn't modelled,
    // so systemd's own test value of 20s is accepted.
    // language="unit file (systemd)"
    val file = """
      [DHCPServer]
      IPv6OnlyPreferredSec=20s
      IPv6OnlyPreferredSec=5min
      IPv6OnlyPreferredSec=300
      IPv6OnlyPreferredSec=1h 5min
      IPv6OnlyPreferredSec=1h30min
      IPv6OnlyPreferredSec=.5h
      IPv6OnlyPreferredSec=10min5s
      IPv6OnlyPreferredSec=infinity
      IPv6OnlyPreferredSec=
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
      IPv6OnlyPreferredSec=yes
      IPv6OnlyPreferredSec=5 fortnights
      IPv6OnlyPreferredSec=-5min
      IPv6OnlyPreferredSec=1h.
      IPv6OnlyPreferredSec=1.2.3
      IPv6OnlyPreferredSec=Infinity
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(6, highlights)
  }
}
