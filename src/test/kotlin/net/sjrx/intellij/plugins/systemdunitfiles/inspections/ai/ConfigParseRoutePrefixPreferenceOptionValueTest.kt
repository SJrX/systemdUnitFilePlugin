package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseRoutePrefixPreferenceOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [IPv6RoutePrefix]
            Preference=high
            Preference=medium
            Preference=low
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
            [IPv6RoutePrefix]
            Preference=invalid
            Preference=normal
            Preference=highest
            Preference=lowest
            Preference=1
            Preference=yes
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(6, highlights)
    }
}
