package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseWireguardPeerRouteTableOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [WireGuardPeer]
            RouteTable=off
            RouteTable=no
            RouteTable=0
            RouteTable=false
            RouteTable=OFF
            RouteTable=No
            RouteTable=fAlSe
            RouteTable=F
            RouteTable=1234
            RouteTable=main
            RouteTable=local
            RouteTable=default
            RouteTable=0x10
            RouteTable=4294967295
            RouteTable=custom
            RouteTable=vpn-table
        """.trimIndent()

        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [WireGuardPeer]
            RouteTable=4294967296
            RouteTable=main table
            RouteTable=custom:100
            RouteTable=00
        """.trimIndent()

        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(4, highlights)
    }
}
