package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseExecOomScoreAdjustOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            OOMScoreAdjust=-1000
            OOMScoreAdjust=0
            OOMScoreAdjust=500
            OOMScoreAdjust=1000
            OOMScoreAdjust=-500
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
            OOMScoreAdjust=<error descr="Invalid option value">-1001</error>
            OOMScoreAdjust=<error descr="Invalid option value">1001</error>
            OOMScoreAdjust=<error descr="Invalid option value">2000</error>
            OOMScoreAdjust=<error descr="Invalid option value">-2000</error>
            OOMScoreAdjust=<error descr="Invalid option value">abc</error>
            OOMScoreAdjust=<error descr="Invalid option value">100.5</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(6, highlights)
    }

    @Test
    fun testBoundaryValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            OOMScoreAdjust=-1000
            OOMScoreAdjust=1000
            OOMScoreAdjust=<error descr="Invalid option value">-1001</error>
            OOMScoreAdjust=<error descr="Invalid option value">1001</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(2, highlights)
    }
}
