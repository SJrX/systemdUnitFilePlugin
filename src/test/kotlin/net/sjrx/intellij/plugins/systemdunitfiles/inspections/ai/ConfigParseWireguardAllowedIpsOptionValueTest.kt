package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseWireguardAllowedIpsOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [WireGuardPeer]
            AllowedIPs=
            AllowedIPs=fd31:bf08:57cb::/48,192.168.26.3/24
            AllowedIPs=0.0.0.0/0
            AllowedIPs=::/0
            AllowedIPs=10.0.0.1
            AllowedIPs=fd8d:4d6d:3ccb:0500:900c:d437:ec27:8822/128
            AllowedIPs=10.0.0.0/8, 192.168.0.0/16
            AllowedIPs=10.0.0.0/8 fd00::/8,
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
            AllowedIPs=10.0.0.0/33
            AllowedIPs=fd00::/129
            AllowedIPs=10.0.0.0/8;192.168.0.0/16
            AllowedIPs=any
            AllowedIPs=10.0.0/8
        """.trimIndent()

        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(5, highlights)
    }
}
