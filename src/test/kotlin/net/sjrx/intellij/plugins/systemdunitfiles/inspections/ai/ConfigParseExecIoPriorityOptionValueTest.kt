package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseExecIoPriorityOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            IOSchedulingPriority=0
            IOSchedulingPriority=4
            IOSchedulingPriority=7
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            IOSchedulingPriority=<error descr="Expecting one of: [INTEGER (0-7)]">8</error>
            IOSchedulingPriority=<error descr="Expecting one of: [INTEGER (0-7)]">-1</error>
            IOSchedulingPriority=<error descr="Expecting one of: [INTEGER (0-7)]">10</error>
            IOSchedulingPriority=<error descr="Expecting one of: [INTEGER (0-7)]">abc</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }

    @Test
    fun testBoundaryValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            IOSchedulingPriority=0
            IOSchedulingPriority=7
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }
}
