package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParse6rdPrefixOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Tunnel]
            IPv6RapidDeploymentPrefix=2001:db8::/32
            IPv6RapidDeploymentPrefix=2001:0db8:85a3::8a2e:0370:7334/64
            IPv6RapidDeploymentPrefix=fe80::1/128
            IPv6RapidDeploymentPrefix=::1/1
            IPv6RapidDeploymentPrefix=2001:db8::/48
            IPv6RapidDeploymentPrefix=fd00::/8
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
            [Tunnel]
            IPv6RapidDeploymentPrefix=2001:db8::/0
            IPv6RapidDeploymentPrefix=2001:db8::
            IPv6RapidDeploymentPrefix=192.168.1.0/24
            IPv6RapidDeploymentPrefix=not-an-address/32
            IPv6RapidDeploymentPrefix=2001:db8::/129
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }

    @Test
    fun testMissingPrefixLength() {
        // Fixture Setup - IPv6 address without prefix length should be invalid
        // language="unit file (systemd)"
        val file = """
            [Tunnel]
            IPv6RapidDeploymentPrefix=2001:db8::1
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }

    @Test
    fun testZeroPrefixLength() {
        // Fixture Setup - Prefix length of 0 is explicitly invalid per C code
        // language="unit file (systemd)"
        val file = """
            [Tunnel]
            IPv6RapidDeploymentPrefix=fe80::/0
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }

    @Test
    fun testVariousValidPrefixLengths() {
        // Fixture Setup - Test boundary values for prefix length
        // language="unit file (systemd)"
        val file = """
            [Tunnel]
            IPv6RapidDeploymentPrefix=2001:db8::/1
            IPv6RapidDeploymentPrefix=2001:db8::/64
            IPv6RapidDeploymentPrefix=2001:db8::/127
            IPv6RapidDeploymentPrefix=2001:db8::/128
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }
}
