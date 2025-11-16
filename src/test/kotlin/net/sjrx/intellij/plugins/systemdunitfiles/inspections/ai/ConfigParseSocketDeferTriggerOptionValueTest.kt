package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseSocketDeferTriggerOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidBooleanValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Socket]
            DeferTrigger=yes
            DeferTrigger=no
            DeferTrigger=true
            DeferTrigger=false
            DeferTrigger=1
            DeferTrigger=0
            DeferTrigger=on
            DeferTrigger=off
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.socket", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidPatientValue() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Socket]
            DeferTrigger=patient
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.socket", file)
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
            [Socket]
            DeferTrigger=invalid
            DeferTrigger=maybe
            DeferTrigger=2
            DeferTrigger=Patient
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.socket", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }
}
