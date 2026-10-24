package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** config_parse_stacked_netdev (all ten kinds). */
class ConfigParseStackedNetdevOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // +0b1 is not an ifindex (safe_atoi gives EINVAL because systemd turns off glibc's C23 0b parsing), so it is a
    // valid name; 0b-1 is the ifindex -1, which is not positive. sd-check accepts both names.
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
            VLAN=0x0
            VLAN=-5
            VLAN=0xvlan
            VLAN=+0b1
            VLAN=0b-1
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
            IPVLAN=0x10
            VLAN=+5
            VLAN=0b1
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(7, myFixture.doHighlighting())
  }
}
