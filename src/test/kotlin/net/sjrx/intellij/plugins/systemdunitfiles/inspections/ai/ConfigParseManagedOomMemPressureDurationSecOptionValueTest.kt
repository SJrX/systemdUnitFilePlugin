package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseManagedOomMemPressureDurationSecOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            ManagedOOMMemoryPressureDurationSec=infinity
            ManagedOOMMemoryPressureDurationSec=10
            ManagedOOMMemoryPressureDurationSec=30s
            ManagedOOMMemoryPressureDurationSec=2min
            ManagedOOMMemoryPressureDurationSec=1h
            ManagedOOMMemoryPressureDurationSec=1min 30s
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
            ManagedOOMMemoryPressureDurationSec=<error descr="Invalid value">abc</error>
            ManagedOOMMemoryPressureDurationSec=<error descr="Invalid value">-1s</error>
            ManagedOOMMemoryPressureDurationSec=<error descr="Invalid value">10zz</error>
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(3, highlights)
    }
}
