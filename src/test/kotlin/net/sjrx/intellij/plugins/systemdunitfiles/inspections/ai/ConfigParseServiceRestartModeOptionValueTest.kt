package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseServiceRestartModeOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Service]
            RestartMode=normal
            RestartMode=direct
            RestartMode=debug
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
            [Service]
            RestartMode=invalid
            RestartMode=auto
            RestartMode=restart
            RestartMode=immediate
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }

    @Test
    fun testValidNormalMode() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Service]
            RestartMode=normal
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidDirectMode() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Service]
            RestartMode=direct
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidDebugMode() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Service]
            RestartMode=debug
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }
}
