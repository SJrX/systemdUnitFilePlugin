package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseVrfTableOptionValueTest : AbstractUnitFileTest() {

    // Values with whitespace are possible table names (networkd.conf can define `my\ table:100`), so they are not
    // flagged even though sd-check, which loads no networkd.conf, reports them. `0b 1`, `0b+1` and `0o 7` are
    // safe_atou32 numbers (sd-check accepts them).
    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [VRF]
            Table=42
            Table=main
            Table=local
            Table=default
            Table=0x2a
            Table=custom-table
            Table=4294967295
            Table=yes
            Table=0x0
            Table=mian
            Table=main 42
            Table=1 2
            Table=my table
            Table=0b 1
            Table=0b+1
            Table=0o 7
        """.trimIndent()

        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [VRF]
            Table=0
            Table=4294967296
            Table=08
            Table=vrf:42
            Table=040000000000
        """.trimIndent()

        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(5, highlights)
    }
}
