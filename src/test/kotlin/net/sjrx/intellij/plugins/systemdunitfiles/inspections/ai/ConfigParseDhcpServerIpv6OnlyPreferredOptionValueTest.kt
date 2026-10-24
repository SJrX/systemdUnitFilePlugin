package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDhcpServerIpv6OnlyPreferredOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Anything >= MIN_V6ONLY_WAIT_USEC (300s) is accepted by systemd's parser (sd-check, 0f0682b773).
    // language="unit file (systemd)"
    val file = """
      [DHCPServer]
      IPv6OnlyPreferredSec=5min
      IPv6OnlyPreferredSec=5m
      IPv6OnlyPreferredSec=4min 60s
      IPv6OnlyPreferredSec=300000000us
      IPv6OnlyPreferredSec=1M
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

  @Test
  fun testBelowMinimumIsInvalid() {
    // systemd: "Invalid [DHCPServer] IPv6OnlyPreferredSec=, ignoring assignment" for every value below
    // 300s outside its test mode (sd-check, 0f0682b773). 20s is systemd's own test-suite value.
    // language="unit file (systemd)"
    val file = """
      [DHCPServer]
      IPv6OnlyPreferredSec=20s
      IPv6OnlyPreferredSec=0
      IPv6OnlyPreferredSec=299
      IPv6OnlyPreferredSec=299.999999s
      IPv6OnlyPreferredSec=4min
      IPv6OnlyPreferredSec=299999999us
      IPv6OnlyPreferredSec=0.0833333h
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(7, highlights)
  }
}
