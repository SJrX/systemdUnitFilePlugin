package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseExecSmackProcessLabelOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language=unit file (systemd)
        val file = """
            [Service]
            SmackProcessLabel=my-label
            SmackProcessLabel=-optional-label
            SmackProcessLabel=label-%i
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Unknown specifier (%e). A trailing bare '%' used to be listed here too, but specifier_printf() keeps it as it
        // is, so it is valid (#568).
        // language=unit file (systemd)
        val file = """
            [Service]
            SmackProcessLabel=bad %e label
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(1, highlights)
    }
}
