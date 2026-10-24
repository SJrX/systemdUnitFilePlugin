package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** config_parse_router_home_agent_lifetime: 0 < t <= 65535s. */
class ConfigParseRouterHomeAgentLifetimeOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [IPv6SendRA]
            HomeAgentLifetimeSec=1
            HomeAgentLifetimeSec=65535
            HomeAgentLifetimeSec=18h 12min 15s
            HomeAgentLifetimeSec=500ms
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
            HomeAgentLifetimeSec=0
            HomeAgentLifetimeSec=65536
            HomeAgentLifetimeSec=infinity
            HomeAgentLifetimeSec=1day
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(4, myFixture.doHighlighting())
  }
}
