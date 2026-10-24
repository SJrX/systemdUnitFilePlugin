package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** config_parse_strv(0): word lists with only quote balance checked. */
class ConfigParseStrvOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValuesNetwork() {
    // language="unit file (systemd)"
    val file = """
            [Network]
            BindCarrier=eth0 eth1
            BindCarrier=en*
            BindCarrier=
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
            [Network]
            BindCarrier=eth0 "eth1
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(1, myFixture.doHighlighting())
  }
  @Test
  fun testValidValuesNspawn() {
    // language="unit file (systemd)"
    val file = """
            [Exec]
            Parameters=/bin/sh -c "echo hello world"
            Parameters='single quoted' plain
            Environment=FOO=bar "BAZ=with space"
            Environment=PATH=/usr/bin\:/bin
        """.trimIndent()

    setupFileInEditor("file.nspawn", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidValuesNspawn() {
    // One highlight per line.
    // language="unit file (systemd)"
    val file = """
            [Exec]
            Parameters=/bin/sh -c "echo
            Environment=FOO='bar
        """.trimIndent()

    setupFileInEditor("file.nspawn", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(2, myFixture.doHighlighting())
  }
}
