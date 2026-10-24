package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** config_parse_router_lifetime: any parse_sec() value (out-of-range values are clamped). */
class ConfigParseRouterLifetimeOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [IPv6SendRA]
            RouterLifetimeSec=1800
            RouterLifetimeSec=0
            RouterLifetimeSec=30min
            RouterLifetimeSec=1s
            RouterLifetimeSec=10000
            RouterLifetimeSec=infinity
            RouterLifetimeSec=.5
            RouterLifetimeSec=+5
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
            [IPv6SendRA]
            RouterLifetimeSec=forever
            RouterLifetimeSec=30 minuts
            RouterLifetimeSec=+.5
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(3, myFixture.doHighlighting())
  }
}
