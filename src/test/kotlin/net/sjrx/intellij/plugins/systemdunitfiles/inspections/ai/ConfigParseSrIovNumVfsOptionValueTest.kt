package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseSrIovNumVfsOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Link]
            SR-IOVVirtualFunctions=0
            SR-IOVVirtualFunctions=1
            SR-IOVVirtualFunctions=100
            SR-IOVVirtualFunctions=2147483647
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.link", file)
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
            [Link]
            SR-IOVVirtualFunctions=<error descr="Invalid option value">2147483648</error>
            SR-IOVVirtualFunctions=<error descr="Invalid option value">-1</error>
            SR-IOVVirtualFunctions=<error descr="Invalid option value">abc</error>
            SR-IOVVirtualFunctions=<error descr="Invalid option value">1.5</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }
}
