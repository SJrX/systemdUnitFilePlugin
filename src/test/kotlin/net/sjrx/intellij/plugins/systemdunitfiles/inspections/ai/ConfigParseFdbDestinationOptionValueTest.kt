package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseFdbDestinationOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidIPv4Addresses() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [BridgeFDB]
            Destination=192.168.1.1
            Destination=10.0.0.1
            Destination=172.16.0.1
            Destination=255.255.255.255
            Destination=0.0.0.0
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
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
            [BridgeFDB]
            Destination=fe80::1
            Destination=2001:db8::1
            Destination=::1
            Destination=2001:0db8:0000:0000:0000:0000:0000:0001
            Destination=2001:db8:85a3::8a2e:370:7334
            Destination=::
            Destination=2001:db8::8a2e:370:7334
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
            [BridgeFDB]
            Destination=256.1.1.1
            Destination=192.168.1
            Destination=not-an-ip
            Destination=192.168.1.1.1
            Destination=gggg::1
            Destination=fe80::gggg
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(6, highlights)
    }

    @Test
    fun testInvalidIPv4Components() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [BridgeFDB]
            Destination=300.168.1.1
            Destination=192.999.1.1
            Destination=192.168.500.1
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(3, highlights)
    }
}
