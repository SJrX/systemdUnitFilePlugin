package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseWireguardEndpointOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [WireGuardPeer]
            Endpoint=192.168.27.3:51820
            Endpoint=[2001:db8::1]:51820
            Endpoint=wg.example.com:51820
            Endpoint=@network.wireguard.peer0.endpoint
            Endpoint=192.168.27.3
            Endpoint=fe80::1
            Endpoint=vpn.example.org.:0x1F90
            Endpoint=[fe80::1%eth0]:51820
            Endpoint=host:65535
            Endpoint=a\255b.example.com:51820
            Endpoint=:51820
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
            Endpoint=wg.example.com
            Endpoint=192.168.27.3:0
            Endpoint=192.168.27.3:65536
            Endpoint=wg..example.com:51820
            Endpoint=[2001:db8::1]:
            Endpoint=wg.example.com:port
            Endpoint=a\999b:51820
        """.trimIndent()

        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(7, highlights)
    }
}
