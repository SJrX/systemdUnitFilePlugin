package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseRouterPreferenceOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [IPv6SendRA]
            RouterPreference=high
            RouterPreference=medium
            RouterPreference=normal
            RouterPreference=default
            RouterPreference=low
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
            [IPv6SendRA]
            RouterPreference=<error descr="Value 'invalid' is not valid">invalid</error>
            RouterPreference=<error descr="Value 'very-high' is not valid">very-high</error>
            RouterPreference=<error descr="Value 'HIGHEST' is not valid">HIGHEST</error>
            RouterPreference=<error descr="Value '0' is not valid">0</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }
}
