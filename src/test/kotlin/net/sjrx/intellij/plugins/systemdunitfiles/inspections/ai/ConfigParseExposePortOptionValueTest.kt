package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseExposePortOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // From TEST-13-NSPAWN; ports are safe_atou16 (base 0).
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
}
