package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParsePrefixOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [IPv6Prefix]
            Prefix=2001:db8::/32
            Prefix=fe80::/64
            Prefix=2001:0db8:85a3:0000:0000:8a2e:0370:7334/128
            Prefix=::1/128
            Prefix=::/0
            Prefix=fd00::/8
            Prefix=2001:db8::1/64
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
            [IPv6Prefix]
            Prefix=2001:db8::
            Prefix=fe80::/
            Prefix=2001:db8::/129
            Prefix=192.168.1.0/24
            Prefix=/64
            Prefix=invalid
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(6, highlights)
    }

    @Test
    fun testEdgeCasePrefixLengths() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [IPv6Prefix]
            Prefix=2001:db8::/0
            Prefix=2001:db8::/64
            Prefix=2001:db8::/128
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testCompressedIPv6Formats() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [IPv6Prefix]
            Prefix=::/0
            Prefix=::1/128
            Prefix=::ffff:192.0.2.1/96
            Prefix=2001:db8::1:0:0:1/64
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }
}
