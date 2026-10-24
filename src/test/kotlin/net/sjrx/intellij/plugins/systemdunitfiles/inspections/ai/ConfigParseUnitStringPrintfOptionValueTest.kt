package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseUnitStringPrintfOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language=unit file (systemd)
        val file = """
            [Unit]
            Description=My Web Service
            Description=Service for %i on %H
            Description=100%% complete
            Description=café and 日本
            Description=Ends with a percent %
            Description=a %-x
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Unknown specifiers (%e, %1) are rejected by unit_full_printf. A trailing bare '%' used to be listed here, but
        // specifier_printf copies a '%' that is not followed by a letter or digit verbatim.
        // language=unit file (systemd)
        val file = """
            [Unit]
            Description=Bad %e specifier
            Description=Bad %1 specifier
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(2, highlights)
    }
}
