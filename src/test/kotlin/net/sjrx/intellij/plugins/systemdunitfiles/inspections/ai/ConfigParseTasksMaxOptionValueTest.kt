package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseTasksMaxOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            TasksMax=infinity
            TasksMax=1
            TasksMax=512
            TasksMax=999999999
            TasksMax=0%
            TasksMax=50%
            TasksMax=99.9%
            TasksMax=12.34%
            TasksMax=100%
            TasksMax=100.0%
            TasksMax=100.00%
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
            TasksMax=<error descr="Invalid value">0</error>
            TasksMax=<error descr="Invalid value">-1</error>
            TasksMax=<error descr="Invalid value">abc</error>
            TasksMax=<error descr="Invalid value">100.5%</error>
            TasksMax=<error descr="Invalid value">250%</error>
            TasksMax=<error descr="Invalid value">50.123%</error>
            TasksMax=<error descr="Invalid value">infinity 1</error>
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(7, highlights)
    }
}
