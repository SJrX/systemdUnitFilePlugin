package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseIrqAffinityNumaOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Link]
            IRQAffinityNUMA=local
            IRQAffinityNUMA=0
            IRQAffinityNUMA=3
            IRQAffinityNUMA=0x1
            IRQAffinityNUMA=4294967293
            IRQAffinityNUMA=0b 1
            IRQAffinityNUMA=0b+1
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
            IRQAffinityNUMA=4294967294
            IRQAffinityNUMA=-1
            IRQAffinityNUMA=remote
            IRQAffinityNUMA=1.5
            IRQAffinityNUMA=Local
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(5, highlights)
    }
}
