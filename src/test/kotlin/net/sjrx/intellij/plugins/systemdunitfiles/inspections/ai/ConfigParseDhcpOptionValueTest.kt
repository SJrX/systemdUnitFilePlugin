package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDhcpOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValueYes() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            DHCP=yes
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidValueNo() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            DHCP=no
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidValueIpv4() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            DHCP=ipv4
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidValueIpv6() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            DHCP=ipv6
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    // config_parse_dhcp falls back to dhcp_deprecated_address_family_table (none/both/v4/v6) with a
    // deprecation warning rather than rejecting the value.
    @Test
    fun testDeprecatedValueBothIsAccepted() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            DHCP=both
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    // address_family_from_string is DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN, so booleans are accepted.
    @Test
    fun testBooleanTrueIsAccepted() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            DHCP=true
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testDeprecatedValueV4IsAccepted() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            DHCP=v4
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testMultipleValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            DHCP=yes
            DHCP=no
            DHCP=ipv4
            DHCP=ipv6
            DHCP=True
            DHCP=oFf
            DHCP=1
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testMultipleInvalidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            DHCP=enabled
            DHCP=all
            DHCP=IPv4
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(3, highlights)
    }
}
