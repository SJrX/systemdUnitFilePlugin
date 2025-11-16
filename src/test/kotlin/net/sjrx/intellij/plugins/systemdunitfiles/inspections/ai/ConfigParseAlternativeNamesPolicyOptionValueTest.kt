package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseAlternativeNamesPolicyOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidSinglePolicies() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Link]
            AlternativeNamesPolicy=database
            AlternativeNamesPolicy=onboard
            AlternativeNamesPolicy=slot
            AlternativeNamesPolicy=path
            AlternativeNamesPolicy=mac
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidMultiplePolicies() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Link]
            AlternativeNamesPolicy=database onboard
            AlternativeNamesPolicy=slot path mac
            AlternativeNamesPolicy=database slot
            AlternativeNamesPolicy=onboard path
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidAllPolicies() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Link]
            AlternativeNamesPolicy=database onboard slot path mac
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidSinglePolicy() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Link]
            AlternativeNamesPolicy=<error descr="Invalid value">invalid</error>
            AlternativeNamesPolicy=<error descr="Invalid value">unknown</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(2, highlights)
    }

    @Test
    fun testInvalidMixedPolicies() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Link]
            AlternativeNamesPolicy=<error descr="Invalid value">database invalid</error>
            AlternativeNamesPolicy=<error descr="Invalid value">onboard bad slot</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(2, highlights)
    }

    @Test
    fun testInvalidNoWhitespace() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Link]
            AlternativeNamesPolicy=<error descr="Invalid value">database,onboard</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }
}
