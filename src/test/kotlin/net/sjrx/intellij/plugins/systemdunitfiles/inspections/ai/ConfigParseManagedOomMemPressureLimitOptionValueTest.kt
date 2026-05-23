package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseManagedOomMemPressureLimitOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            ManagedOOMMemoryPressureLimit=0%
            ManagedOOMMemoryPressureLimit=50%
            ManagedOOMMemoryPressureLimit=99.9%
            ManagedOOMMemoryPressureLimit=12.34%
            ManagedOOMMemoryPressureLimit=100%
            ManagedOOMMemoryPressureLimit=100.0%
            ManagedOOMMemoryPressureLimit=100.00%
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
            ManagedOOMMemoryPressureLimit=<error descr="Invalid value">50</error>
            ManagedOOMMemoryPressureLimit=<error descr="Invalid value">100.1%</error>
            ManagedOOMMemoryPressureLimit=<error descr="Invalid value">250%</error>
            ManagedOOMMemoryPressureLimit=<error descr="Invalid value">abc</error>
            ManagedOOMMemoryPressureLimit=<error descr="Invalid value">12.345%</error>
            ManagedOOMMemoryPressureLimit=<error descr="Invalid value">-5%</error>
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(6, highlights)
    }
}
