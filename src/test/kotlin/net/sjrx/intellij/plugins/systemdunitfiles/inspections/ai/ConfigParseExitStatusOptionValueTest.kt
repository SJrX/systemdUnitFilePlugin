package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseExitStatusOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Unit]
            FailureActionExitStatus=0
            FailureActionExitStatus=1
            FailureActionExitStatus=127
            FailureActionExitStatus=255
            SuccessActionExitStatus=0
            SuccessActionExitStatus=42
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
            FailureActionExitStatus=<error descr="Invalid value">256</error>
            FailureActionExitStatus=<error descr="Invalid value">-1</error>
            FailureActionExitStatus=<error descr="Invalid value">300</error>
            SuccessActionExitStatus=<error descr="Invalid value">1000</error>
            SuccessActionExitStatus=<error descr="Invalid value">invalid</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }

    @Test
    fun testBoundaryValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Unit]
            FailureActionExitStatus=0
            FailureActionExitStatus=255
            SuccessActionExitStatus=<error descr="Invalid value">256</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }
}
