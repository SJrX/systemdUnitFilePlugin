package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseOomScoreAdjustOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarEngineVerdict
import org.junit.Assert.assertEquals
import org.junit.Test

class ConfigParseOomScoreAdjustOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // safe_atoi reads base 0 after mangle_base: sign, hex, octal, 0o and 0b all work; range is -1000..1000.
        // language="unit file (systemd)"
        val file = """
            [Exec]
            OOMScoreAdjust=32
            OOMScoreAdjust=-1000
            OOMScoreAdjust=1000
            OOMScoreAdjust=0
            OOMScoreAdjust=+5
            OOMScoreAdjust=0x10
            OOMScoreAdjust=-0x3e8
            OOMScoreAdjust=0o17
            OOMScoreAdjust=0b101
            OOMScoreAdjust=017
            OOMScoreAdjust=-0
            OOMScoreAdjust=0b-1
            OOMScoreAdjust=0o+120
            OOMScoreAdjust=0O-1750
            OOMScoreAdjust=0b 1
            OOMScoreAdjust=0o  -7
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Out of range (also when spelled in hex), not a number, a sign defeating mangle_base's 0b prefix, and
        // whitespace strtol() does not skip (after a sign, after 0x, between a sign and the digits).
        // language="unit file (systemd)"
        val file = """
            [Exec]
            OOMScoreAdjust=1001
            OOMScoreAdjust=-1001
            OOMScoreAdjust=abc
            OOMScoreAdjust=0x3e9
            OOMScoreAdjust=1.5
            OOMScoreAdjust=+0b1
            OOMScoreAdjust=0b-1111101001
            OOMScoreAdjust=- 5
            OOMScoreAdjust=0x 5
            OOMScoreAdjust=0b - 1
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(10, highlights)
    }

    @Test
    fun testLeadingVerticalTabAndFormFeed() {
        // config_parse strips " \t\n\r" but not \v or \f, and strtol() skips them, so sd-check accepts
        // OOMScoreAdjust=\v5, \v-5 and \f-1000. The editor's lexer does not take \v right after `=`, so these are
        // checked on the grammar directly, on both engines.
        val grammar = ConfigParseOomScoreAdjustOptionValue().combinator
        for (value in listOf("\u000B5", "\u000B-5", "\u000C-1000")) {
            assertEquals("classic engine on <$value>", GrammarEngineVerdict.VALID, GrammarEngineVerdict.classic(grammar, value))
            assertEquals("parse engine on <$value>", GrammarEngineVerdict.VALID, GrammarEngineVerdict.parseEngine(grammar, value))
        }
    }
}
