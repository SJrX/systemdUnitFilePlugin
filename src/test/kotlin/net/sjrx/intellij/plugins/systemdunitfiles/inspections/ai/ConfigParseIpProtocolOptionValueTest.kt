package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseIpProtocolOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [FooOverUDP]
            Protocol=tcp
            Protocol=udp
            Protocol=icmp
            Protocol=icmpv6
            Protocol=sctp
            Protocol=0
            Protocol=47
            Protocol=255
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
            [FooOverUDP]
            Protocol=<error descr="Value must satisfy: config_parse_ip_protocol">256</error>
            Protocol=<error descr="Value must satisfy: config_parse_ip_protocol">-1</error>
            Protocol=<error descr="Value must satisfy: config_parse_ip_protocol">not-a-protocol</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(3, highlights)
    }
}
