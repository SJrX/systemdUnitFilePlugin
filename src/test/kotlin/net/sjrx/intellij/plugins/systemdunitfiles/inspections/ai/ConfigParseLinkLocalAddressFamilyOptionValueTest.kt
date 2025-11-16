package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseLinkLocalAddressFamilyOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidBooleanValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            LinkLocalAddressing=yes
            LinkLocalAddressing=no
            LinkLocalAddressing=true
            LinkLocalAddressing=false
            LinkLocalAddressing=1
            LinkLocalAddressing=0
            LinkLocalAddressing=on
            LinkLocalAddressing=off
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidAddressFamilyValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            LinkLocalAddressing=ipv4
            LinkLocalAddressing=ipv6
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidCompatibilityNames() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            LinkLocalAddressing=fallback
            LinkLocalAddressing=fallback-ipv4
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
            [Network]
            LinkLocalAddressing=invalid
            LinkLocalAddressing=both
            LinkLocalAddressing=ipv4-ipv6
            LinkLocalAddressing=2
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }
}
