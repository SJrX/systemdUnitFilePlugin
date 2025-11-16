package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseArpIpTargetAddressOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidSingleAddress() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Bond]
            ARPIPTargets=192.168.1.1
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidMultipleAddresses() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Bond]
            ARPIPTargets=192.168.1.1 10.0.0.1 172.16.0.1
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidEdgeCaseAddresses() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Bond]
            ARPIPTargets=0.0.0.0 255.255.255.255
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidIPv6Address() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Bond]
            ARPIPTargets=<error descr="Invalid value: 2001:db8::1">2001:db8::1</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }

    @Test
    fun testInvalidOctetTooLarge() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Bond]
            ARPIPTargets=<error descr="Invalid value: 192.168.1.256">192.168.1.256</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }

    @Test
    fun testInvalidHostname() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Bond]
            ARPIPTargets=<error descr="Invalid value: gateway.example.com">gateway.example.com</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }

    @Test
    fun testInvalidIncompleteAddress() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Bond]
            ARPIPTargets=<error descr="Invalid value: 192.168.1">192.168.1</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }

    @Test
    fun testMixedValidAndInvalid() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Bond]
            ARPIPTargets=192.168.1.1 <error descr="Invalid value: invalid">invalid</error> 10.0.0.1
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }
}
