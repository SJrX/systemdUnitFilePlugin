package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDhcp6PdPrefixHintOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [DHCPv6]
            PrefixDelegationHint=2001:db8::/64
            PrefixDelegationHint=fe80::1/128
            PrefixDelegationHint=::1/1
            PrefixDelegationHint=2001:db8:1234:5678::/64
            PrefixDelegationHint=2001:db8::1/48
            PrefixDelegationHint=fd00::/8
            PrefixDelegationHint=::/1
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
            [DHCPv6]
            PrefixDelegationHint=2001:db8::/0
            PrefixDelegationHint=fe80::1/129
            PrefixDelegationHint=192.168.1.0/24
            PrefixDelegationHint=2001:db8::
            PrefixDelegationHint=not-an-address/64
            PrefixDelegationHint=2001:db8::/
            PrefixDelegationHint=2001:db8::/256
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(7, highlights)
    }

    @Test
    fun testEdgeCasePrefixLengths() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [DHCPv6]
            PrefixDelegationHint=2001:db8::/1
            PrefixDelegationHint=2001:db8::/128
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testIPv6WithZeroCompression() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [DHCPv6]
            PrefixDelegationHint=2001:db8::1/64
            PrefixDelegationHint=::ffff:192.0.2.1/96
            PrefixDelegationHint=fe80::/10
            PrefixDelegationHint=ff00::/8
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }
}
