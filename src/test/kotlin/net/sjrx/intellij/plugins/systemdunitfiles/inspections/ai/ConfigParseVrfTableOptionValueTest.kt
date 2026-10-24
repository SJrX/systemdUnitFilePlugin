package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseVrfTableOptionValueTest : AbstractUnitFileTest() {

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
            Table=main 42
            Table=vrf:42
            Table=1 2
        """.trimIndent()

        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(5, highlights)
    }
}
