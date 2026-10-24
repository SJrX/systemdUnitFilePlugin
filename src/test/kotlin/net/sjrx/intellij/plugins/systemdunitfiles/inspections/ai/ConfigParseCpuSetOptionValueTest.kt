package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseCpuSetOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Link]
            IRQAffinity=
            IRQAffinity=0
            IRQAffinity=1 2 4
            IRQAffinity=0-3,8-11
            IRQAffinity=0, 1, 2, 3, 4, 5, 6, 7, 63, 
            IRQAffinity=8 '9' 10 "11"
            IRQAffinity=0-3  8-11, 
            IRQAffinity=8000-8191
            IRQAffinity=0x0-0x3
            IRQAffinity=,0
            IRQAffinity=0-"3"
            IRQAffinity='0'-'3', 4
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Link]
            IRQAffinity=0 1 2 3 garbage
            IRQAffinity=0-3 8-garbage
            IRQAffinity=8192
            IRQAffinity=1-
            IRQAffinity=-1
            IRQAffinity=0 1 2 3 "4 5 6 7 
            IRQAffinity=-0
            IRQAffinity=0-"9000"
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(8, highlights)
    }
}
