package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** config_parse_router_uint32_msec_usec. */
class ConfigParseRouterUint32MsecUsecOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [IPv6SendRA]
            ReachableTimeSec=30s
            ReachableTimeSec=0
            ReachableTimeSec=1h30min
            ReachableTimeSec=1500ms
            ReachableTimeSec=4294967295ms
            ReachableTimeSec=infinity
            RetransmitSec=1.5
            RetransmitSec=.5s
            RetransmitSec=2 min
            RetransmitSec=1d 2h
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
            ReachableTimeSec=4294967296ms
            ReachableTimeSec=50d
            RetransmitSec=5x
            RetransmitSec=-1s
            RetransmitSec=5.
            RetransmitSec=12.34.56
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(6, myFixture.doHighlighting())
  }
}
