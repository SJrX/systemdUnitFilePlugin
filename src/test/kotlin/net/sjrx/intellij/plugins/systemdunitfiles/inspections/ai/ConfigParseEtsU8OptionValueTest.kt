package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseEtsU8OptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [EnhancedTransmissionSelection]
            Bands=1
            Bands=8
            Bands=16
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
            [EnhancedTransmissionSelection]
            Bands=<error descr="Value does not satisfy grammar for 'config_parse_ets_u8'">0</error>
            Bands=<error descr="Value does not satisfy grammar for 'config_parse_ets_u8'">17</error>
            Bands=<error descr="Value does not satisfy grammar for 'config_parse_ets_u8'">100</error>
            Bands=<error descr="Value does not satisfy grammar for 'config_parse_ets_u8'">abc</error>
            Bands=<error descr="Value does not satisfy grammar for 'config_parse_ets_u8'">-1</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }
}
