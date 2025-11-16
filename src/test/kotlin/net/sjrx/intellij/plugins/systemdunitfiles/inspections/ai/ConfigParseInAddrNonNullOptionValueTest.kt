package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseInAddrNonNullOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidIPv4Addresses() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [DHCPServer]
            RelayTarget=192.168.1.1
            RelayTarget=10.0.0.1
            RelayTarget=172.16.0.1
            RelayTarget=8.8.8.8
            RelayTarget=255.255.255.255
            RelayTarget=1.2.3.4
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
            RelayTarget=192.168.1
            RelayTarget=256.1.1.1
            RelayTarget=192.168.1.1.1
            RelayTarget=not-an-ip
            RelayTarget=192.168.-1.1
            RelayTarget=192.168.1.1/24
            RelayTarget=::1
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(7, highlights)
    }

    @Test
    fun testBoundaryValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [DHCPServer]
            RelayTarget=0.0.0.0
            RelayTarget=127.0.0.1
            RelayTarget=255.0.0.0
            RelayTarget=0.0.0.1
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        // All are syntactically valid IPv4 addresses
        // Note: 0.0.0.0 would be rejected semantically by the C code, but is syntactically valid
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidOctetRanges() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [DHCPServer]
            RelayTarget=256.0.0.1
            RelayTarget=192.256.1.1
            RelayTarget=192.168.256.1
            RelayTarget=192.168.1.256
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }
}
