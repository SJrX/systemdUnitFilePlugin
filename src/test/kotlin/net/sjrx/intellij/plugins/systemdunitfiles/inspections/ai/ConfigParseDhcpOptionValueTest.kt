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

    @Test
    fun testInvalidValueBoth() {
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
        assertSize(1, highlights)
    }

    @Test
    fun testInvalidValueTrue() {
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
        assertSize(1, highlights)
    }

    @Test
    fun testInvalidValueV4() {
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
        assertSize(1, highlights)
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
            DHCP=v6
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(3, highlights)
    }
}
