package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseFouTunnelAddressOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidIPv4Addresses() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [FooOverUDP]
            Peer=192.168.1.1
            Peer=10.0.0.1
            Peer=172.16.0.1
            Peer=255.255.255.255
            Peer=0.0.0.0
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidIPv6Addresses() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [FooOverUDP]
            Peer=2001:db8::1
            Peer=fe80::1
            Peer=::1
            Peer=::
            Peer=2001:db8:85a3::8a2e:370:7334
            Peer=2001:db8:85a3:0:0:8a2e:370:7334
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidIPv6WithIPv4Suffix() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [FooOverUDP]
            Peer=::ffff:192.168.1.1
            Peer=64:ff9b::192.0.2.1
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidIPAddresses() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [FooOverUDP]
            Peer=<error descr="Invalid option value">256.1.1.1</error>
            Peer=<error descr="Invalid option value">192.168.1</error>
            Peer=<error descr="Invalid option value">192.168.1.1.1</error>
            Peer=<error descr="Invalid option value">not-an-ip</error>
            Peer=<error descr="Invalid option value">localhost</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }

    @Test
    fun testInvalidIPv6Addresses() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [FooOverUDP]
            Peer=<error descr="Invalid option value">gggg::1</error>
            Peer=<error descr="Invalid option value">2001:db8::1::2</error>
            Peer=<error descr="Invalid option value">2001:db8::</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(3, highlights)
    }

    @Test
    fun testInvalidWithPrefixLength() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [FooOverUDP]
            Peer=<error descr="Invalid option value">192.168.1.1/24</error>
            Peer=<error descr="Invalid option value">2001:db8::1/64</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(2, highlights)
    }
}
