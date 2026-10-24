package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseWireguardPeerKeyFileOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [WireGuardPeer]
            PublicKeyFile=/run/systemd/network/25-wireguard-public-key.txt
            PresharedKeyFile=/etc/systemd/network/wg psk.key
            PresharedKeyFile=/etc//systemd/./network/psk/
            PublicKeyFile=/devices/key
            PublicKeyFile=/run/credentials/%i.key
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
            PublicKeyFile=relative/key
            PresharedKeyFile=/etc/../root/psk
            PublicKeyFile=/proc/self/environ
            PresharedKeyFile=/dev/stdin
            PublicKeyFile=%d/key
            PresharedKeyFile=//sys/key
        """.trimIndent()

        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(6, highlights)
    }
}
