package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseExposePortOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarEngineVerdict
import org.junit.Assert.assertEquals
import org.junit.Test

class ConfigParseExposePortOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // From TEST-13-NSPAWN; ports are safe_atou16 (base 0). A `-` after the 0b prefix escapes safe_atou's minus
        // check and strtoul() negates modulo 2^64, so `0b -<2^64-80 in binary>` is port 80 (sd-check accepts it).
        // language="unit file (systemd)"
        val file = """
            [Network]
            Port=80
            Port=81:8181
            Port=tcp:60
            Port=udp:60:61
            Port=65535
            Port=0x50
            Port=tcp:0x50:010
            Port=0o+120
            Port=0b 1
            Port=80:0b 1
            Port=tcp:0o 120
            Port=0b -$PORT_80_WRAPPED
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Rejected spellings from TEST-13-NSPAWN (--port=-1, :, icmp:..., tcp::8080, 8080:) plus port 0, overflow,
        // an upper-case protocol (startswith is case-sensitive), a third field, and whitespace around a port (parse_ip_port
        // refuses leading whitespace; only the whitespace strtoul() skips after a 0b/0o prefix is fine).
        // language="unit file (systemd)"
        val file = """
            [Network]
            Port=0
            Port=-1
            Port=:
            Port=icmp:80:8080
            Port=tcp::8080
            Port=8080:
            Port=65536
            Port=TCP:80
            Port=80:8080:1
            Port=80: 90
            Port=tcp: 80
            Port=80 :90
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(12, highlights)
    }

    @Test
    fun testLeadingVerticalTab() {
        // SAFE_ATO_REFUSE_LEADING_WHITESPACE only checks " \t\n\r", so a \v reaches strtoul(), which skips it.
        // sd-check accepts Port=\v80 and Port=tcp:\v80:81 but rejects Port=tcp:80: 81. The editor's lexer does not take
        // \v right after `=`, so these are checked on the grammar directly, on both engines.
        val grammar = ConfigParseExposePortOptionValue().combinator
        for (value in listOf("\u000B80", "tcp:\u000B80:81", "80:\u000B81")) {
            assertEquals("classic engine on <$value>", GrammarEngineVerdict.VALID, GrammarEngineVerdict.classic(grammar, value))
            assertEquals("parse engine on <$value>", GrammarEngineVerdict.VALID, GrammarEngineVerdict.parseEngine(grammar, value))
        }
        for (value in listOf("tcp:80: 81", "tcp:\t80")) {
            assertEquals("classic engine on <$value>", GrammarEngineVerdict.Kind.SYNTAX, GrammarEngineVerdict.classic(grammar, value).kind)
            assertEquals("parse engine on <$value>", GrammarEngineVerdict.Kind.SYNTAX, GrammarEngineVerdict.parseEngine(grammar, value).kind)
        }
    }
}

/** 2^64 - 80 in binary: strtoul() negates it modulo 2^64 to 80. */
private val PORT_80_WRAPPED = (java.math.BigInteger.ONE.shiftLeft(64) - java.math.BigInteger.valueOf(80)).toString(2)
