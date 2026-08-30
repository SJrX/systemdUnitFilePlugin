package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseUnitConditionPressureOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Covers all three pressure ltypes (memory/cpu/io) plus the slice, spacing, and timespan forms.
        // language="unit file (systemd)"
        val file = """
            [Unit]
            ConditionMemoryPressure=20%/5min
            ConditionMemoryPressure=10%/10sec
            ConditionMemoryPressure=50%
            ConditionCPUPressure=12%/1min
            ConditionIOPressure=30% / 1min
            ConditionMemoryPressure=system.slice:20%/5min
            ConditionMemoryPressure=system.slice : 40%
            AssertCPUPressure=12%
            AssertIOPressure=30%/5min
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testValidWithMarkers() {
        // language="unit file (systemd)"
        val file = """
            [Unit]
            ConditionMemoryPressure=!20%/5min
            ConditionMemoryPressure=|50%
            ConditionMemoryPressure=|!30%/1min
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
            [Unit]
            ConditionMemoryPressure=20%/5sec
            ConditionMemoryPressure=30 %/1min
            ConditionMemoryPressure=abc
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(3, highlights)
    }
}
