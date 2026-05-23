package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseExecCpuAffinityOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            CPUAffinity=numa
            CPUAffinity=0
            CPUAffinity=0-3
            CPUAffinity=0,2,4
            CPUAffinity=0-3 5
            CPUAffinity=0-3,5,8-11
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            CPUAffinity=<error descr="Invalid value">all</error>
            CPUAffinity=<error descr="Invalid value">numa 0</error>
            CPUAffinity=<error descr="Invalid value">-1</error>
            CPUAffinity=<error descr="Invalid value">0-</error>
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(4, highlights)
    }
}
