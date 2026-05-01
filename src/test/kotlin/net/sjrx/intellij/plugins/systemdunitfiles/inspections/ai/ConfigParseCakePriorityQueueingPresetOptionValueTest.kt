package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseCakePriorityQueueingPresetOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [CAKE]
            PriorityQueueingPreset=besteffort
            PriorityQueueingPreset=precedence
            PriorityQueueingPreset=diffserv3
            PriorityQueueingPreset=diffserv4
            PriorityQueueingPreset=diffserv8
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [CAKE]
            PriorityQueueingPreset=diffserv5
            PriorityQueueingPreset=invalid
            PriorityQueueingPreset=diffserv
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()
        assertSize(3, highlights)
    }
}
