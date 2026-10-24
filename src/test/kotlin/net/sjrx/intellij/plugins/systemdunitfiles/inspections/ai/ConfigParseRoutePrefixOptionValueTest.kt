package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** [IPv6RoutePrefix] Route=. */
class ConfigParseRoutePrefixOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [IPv6RoutePrefix]
            Route=2001:db0:fff::/64
            Route=2001:db2:fff::/48
            Route=::/0
            Route=2001:db8::1
            Route=2001:db8::/0x40
            Route=2001:db8::/ 64
            Route=2001:db8::/0b 1
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
            [IPv6RoutePrefix]
            Route=192.168.0.0/24
            Route=2001:db8::/129
            Route=2001:db8::/
            Route=2001:db8::/+0b1
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(4, myFixture.doHighlighting())
  }
}
