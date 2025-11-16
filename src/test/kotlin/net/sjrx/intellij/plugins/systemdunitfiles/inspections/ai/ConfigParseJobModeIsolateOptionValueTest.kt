package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseJobModeIsolateOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidBooleanTrueValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Unit]
            OnFailureIsolate=yes
            OnFailureIsolate=true
            OnFailureIsolate=1
            OnFailureIsolate=on
            OnFailureIsolate=y
            OnFailureIsolate=t
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidBooleanFalseValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Unit]
            OnFailureIsolate=no
            OnFailureIsolate=false
            OnFailureIsolate=0
            OnFailureIsolate=off
            OnFailureIsolate=n
            OnFailureIsolate=f
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.service", file)
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
            [Unit]
            OnFailureIsolate=invalid
            OnFailureIsolate=maybe
            OnFailureIsolate=2
            OnFailureIsolate=yesno
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }
}
