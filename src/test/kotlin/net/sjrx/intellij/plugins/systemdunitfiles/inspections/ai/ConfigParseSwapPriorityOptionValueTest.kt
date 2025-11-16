package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseSwapPriorityOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            Priority=-1
            Priority=0
            Priority=100
            Priority=32767
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
            Priority=<error descr="Invalid value">-2</error>
            Priority=<error descr="Invalid value">-100</error>
            Priority=<error descr="Invalid value">32768</error>
            Priority=<error descr="Invalid value">100000</error>
            Priority=<error descr="Invalid value">invalid</error>
            Priority=<error descr="Invalid value">1.5</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(6, highlights)
    }

    @Test
    fun testEdgeCases() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            Priority=-1
            Priority=32767
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }
}
