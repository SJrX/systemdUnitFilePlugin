package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseBootOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidBooleanValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Exec]
            Boot=yes
            Boot=no
            Boot=1
            Boot=0
            Boot=true
            Boot=false
            Boot=on
            Boot=off
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidBooleanValuesAllVariants() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Exec]
            Boot=y
            Boot=n
            Boot=t
            Boot=f
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.nspawn", file)
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
            [Exec]
            Boot=invalid
            Boot=2
            Boot=maybe
            Boot=yep
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }
}
