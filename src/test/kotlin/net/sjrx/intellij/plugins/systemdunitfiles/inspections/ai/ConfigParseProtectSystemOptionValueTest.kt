package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseProtectSystemOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidBooleanValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            ProtectSystem=yes
            ProtectSystem=no
            ProtectSystem=true
            ProtectSystem=false
            ProtectSystem=on
            ProtectSystem=off
            ProtectSystem=1
            ProtectSystem=0
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidSpecialValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            ProtectSystem=full
            ProtectSystem=strict
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
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
            [Swap]
            ProtectSystem=partial
            ProtectSystem=maybe
            ProtectSystem=2
            ProtectSystem=complete
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }

    @Test
    fun testShortBooleanValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            ProtectSystem=y
            ProtectSystem=n
            ProtectSystem=t
            ProtectSystem=f
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }
}
