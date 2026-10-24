package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** config_parse_stacked_netdev (all ten kinds). */
class ConfigParseStackedNetdevOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [Network]
            IPoIB=ib0.8001
            IPVLAN=ipvlan99
            IPVTAP=ipvtap99
            MACsec=macsec99
            MACVLAN=macvlan99 macvlan98
            MACVTAP=macvtap99
            Tunnel=sittun99
            VLAN=vlan99
            VLAN=vlan-1 vlan-2
            VXLAN=vxlan-slaac
            Xfrm=xfrm98
            VLAN=
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
            [Network]
            VLAN=vlan:1
            VXLAN=this-name-is-way-too-long
            Tunnel=123
            MACVLAN=ok all
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(4, myFixture.doHighlighting())
  }
}
