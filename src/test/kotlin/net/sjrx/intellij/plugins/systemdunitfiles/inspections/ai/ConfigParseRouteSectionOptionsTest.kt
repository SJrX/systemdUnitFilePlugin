package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/**
 * [Route] section options under config_parse_route_section (#476). These were all mis-registered to
 * the route-Type enum grammar, so valid integers/booleans/enums were wrongly flagged. Each ltype now
 * has its correct grammar.
 */
class ConfigParseRouteSectionOptionsTest : AbstractUnitFileTest() {

  @Test
  fun testValidRouteOptions() {
    // language="unit file (systemd)"
    val file = """
            [Route]
            Type=unicast
            Metric=100
            Protocol=static
            Protocol=42
            IPv6Preference=high
            NextHop=5
            HopLimit=64
            InitialAdvertisedReceiveWindow=10
            QuickAck=true
            FastOpenNoCookie=yes
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)

    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidRouteOptions() {
    // One bad value per line: non-number metric, bad preference, out-of-range protocol/hoplimit,
    // non-boolean quickack -> one highlight each.
    // language="unit file (systemd)"
    val file = """
            [Route]
            Metric=notanumber
            IPv6Preference=sideways
            Protocol=999
            HopLimit=0
            QuickAck=perhaps
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)

    assertSize(5, myFixture.doHighlighting())
  }
}
