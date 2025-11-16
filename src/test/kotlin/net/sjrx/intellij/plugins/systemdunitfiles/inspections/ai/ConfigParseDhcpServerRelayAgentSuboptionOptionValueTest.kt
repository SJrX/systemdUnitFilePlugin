package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDhcpServerRelayAgentSuboptionOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [DHCPServer]
            RelayAgentRemoteId=string:some-remote-id
            RelayAgentRemoteId=string:12345
            RelayAgentRemoteId=string:
            RelayAgentRemoteId=string:value with spaces
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
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
            [DHCPServer]
            RelayAgentRemoteId=<error descr="Invalid value">invalid</error>
            RelayAgentRemoteId=<error descr="Invalid value">str:value</error>
            RelayAgentRemoteId=<error descr="Invalid value">STRING:value</error>
            RelayAgentRemoteId=<error descr="Invalid value">123</error>
            RelayAgentRemoteId=<error descr="Invalid value">value:string</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }
}
