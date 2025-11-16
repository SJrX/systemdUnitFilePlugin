package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseCakeOverheadOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [CAKE]
            OverheadBytes=-64
            OverheadBytes=-1
            OverheadBytes=0
            OverheadBytes=1
            OverheadBytes=38
            OverheadBytes=256
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
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
            [CAKE]
            OverheadBytes=<error descr="Invalid option value">-65</error>
            OverheadBytes=<error descr="Invalid option value">257</error>
            OverheadBytes=<error descr="Invalid option value">1000</error>
            OverheadBytes=<error descr="Invalid option value">-100</error>
            OverheadBytes=<error descr="Invalid option value">abc</error>
            OverheadBytes=<error descr="Invalid option value">12.5</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
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
            [CAKE]
            OverheadBytes=-64
            OverheadBytes=256
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }
}
