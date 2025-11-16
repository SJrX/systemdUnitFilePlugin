package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseIpReversePathFilterOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            IPv4ReversePathFilter=no
            IPv4ReversePathFilter=strict
            IPv4ReversePathFilter=loose
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
            IPv4ReversePathFilter=yes
            IPv4ReversePathFilter=invalid
            IPv4ReversePathFilter=true
            IPv4ReversePathFilter=1
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }
}
