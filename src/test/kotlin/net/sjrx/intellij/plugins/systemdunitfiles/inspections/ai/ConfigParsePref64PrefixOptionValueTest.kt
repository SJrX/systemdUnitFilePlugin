package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParsePref64PrefixOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [IPv6PREF64Prefix]
            Prefix=2001:db8::/32
            Prefix=2001:db8::/40
            Prefix=2001:db8::/48
            Prefix=2001:db8::/56
            Prefix=2001:db8::/64
            Prefix=2001:db8::/96
            Prefix=64:ff9b::/96
            Prefix=::ffff:0:0/96
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidPrefixLengths() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [IPv6PREF64Prefix]
            Prefix=<error descr="Value does not match expected format">2001:db8::/128</error>
            Prefix=<error descr="Value does not match expected format">2001:db8::/24</error>
            Prefix=<error descr="Value does not match expected format">2001:db8::/16</error>
            Prefix=<error descr="Value does not match expected format">2001:db8::/8</error>
            Prefix=<error descr="Value does not match expected format">2001:db8::/80</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }

    @Test
    fun testMissingPrefixLength() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [IPv6PREF64Prefix]
            Prefix=<error descr="Value does not match expected format">2001:db8::</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }

    @Test
    fun testInvalidIPv6Address() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [IPv6PREF64Prefix]
            Prefix=<error descr="Value does not match expected format">192.168.1.0/96</error>
            Prefix=<error descr="Value does not match expected format">not-an-address/96</error>
            Prefix=<error descr="Value does not match expected format">gggg::/96</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(3, highlights)
    }

    @Test
    fun testMissingSeparator() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [IPv6PREF64Prefix]
            Prefix=<error descr="Value does not match expected format">2001:db8::96</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }
}
