package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseBareUdpIftypeOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [BareUDP]
            EtherType=ipv4
            EtherType=ipv6
            EtherType=mpls-uc
            EtherType=mpls-mc
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [BareUDP]
            EtherType=<error descr="Value must satisfy: config_parse_bare_udp_iftype">ipv8</error>
            EtherType=<error descr="Value must satisfy: config_parse_bare_udp_iftype">tcp</error>
            EtherType=<error descr="Value must satisfy: config_parse_bare_udp_iftype">mpls</error>
            EtherType=<error descr="Value must satisfy: config_parse_bare_udp_iftype">ethernet</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }

    @Test
    fun testPartialMatches() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [BareUDP]
            EtherType=<error descr="Value must satisfy: config_parse_bare_udp_iftype">ipv</error>
            EtherType=<error descr="Value must satisfy: config_parse_bare_udp_iftype">mpls-</error>
            EtherType=<error descr="Value must satisfy: config_parse_bare_udp_iftype">uc</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(3, highlights)
    }
}
