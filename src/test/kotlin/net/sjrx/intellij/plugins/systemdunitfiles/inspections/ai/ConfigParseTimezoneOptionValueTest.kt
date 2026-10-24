package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** config_parse_timezone (syntax of verify_timezone). */
class ConfigParseTimezoneOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Nowhere/Fake is well-formed; systemd only rejects it because /usr/share/zoneinfo/Nowhere/Fake does not exist,
    // which depends on the installed tzdata (deliberate leniency).
    // language="unit file (systemd)"
    val file = """
            [DHCPServer]
            Timezone=Europe/Berlin
            Timezone=UTC
            Timezone=America/Argentina/Buenos_Aires
            Timezone=Etc/GMT+5
            Timezone=Etc/GMT-14
            Timezone=Nowhere/Fake
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidValues() {
    // One highlight per line.
    // language="unit file (systemd)"
    val file = """
            [DHCPServer]
            Timezone=/Europe/Berlin
            Timezone=Europe/
            Timezone=Europe//Berlin
            Timezone=../etc/passwd
            Timezone=Europe/Berlin Time
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(5, myFixture.doHighlighting())
  }
}
