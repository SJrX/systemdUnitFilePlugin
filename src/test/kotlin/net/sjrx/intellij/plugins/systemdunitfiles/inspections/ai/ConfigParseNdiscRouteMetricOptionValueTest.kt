package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** config_parse_ndisc_route_metric. */
class ConfigParseNdiscRouteMetricOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [IPv6AcceptRA]
            RouteMetric=256
            RouteMetric=512:1024:2048
            RouteMetric=100:200:300
            RouteMetric=0
            RouteMetric=0x10:0x20:0x30
            RouteMetric=100: 200:	300
            RouteMetric=100:200:300:
            RouteMetric=0b 1
            RouteMetric=0b1:0b 10:0b11
            [DHCPv6]
            RouteMetric=1024
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidValues() {
    // One highlight per line. sd-check rejects +0b1 and 1:+0b10:3, and accepts 0b 1 and 0b1:0b 10:0b11.
    // language="unit file (systemd)"
    val file = """
            [IPv6AcceptRA]
            RouteMetric=hoge
            RouteMetric=300:200:100
            RouteMetric=100:100:200
            RouteMetric=1:2
            RouteMetric=1:2:3:4
            RouteMetric=1::3
            RouteMetric=1:2:3::
            RouteMetric=100 :200:300
            RouteMetric=+0b1
            RouteMetric=1:+0b10:3
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(10, myFixture.doHighlighting())
  }
}
