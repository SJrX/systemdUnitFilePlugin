package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseWireguardRouteTableOptionValueTest : AbstractUnitFileTest() {

    // Values with whitespace are possible table names (networkd.conf can define `my\ table:100`), so they are not
    // flagged even though sd-check, which loads no networkd.conf, reports them. `0b 1`, `0b+1` and `0o 7` are
    // safe_atou32 numbers (sd-check accepts them).
    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [WireGuard]
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
            RouteTable=yes
            RouteTable=main table
            RouteTable=0b 1
            RouteTable=0b+1
            RouteTable=0o 7
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
            [WireGuard]
            RouteTable=4294967296
            RouteTable=08
            RouteTable=custom:100
            RouteTable=00
        """.trimIndent()

        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(4, highlights)
    }
}
