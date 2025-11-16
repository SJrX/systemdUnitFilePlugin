package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseErspanHwidOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Tunnel]
            ERSPANHardwareId=0
            ERSPANHardwareId=1
            ERSPANHardwareId=32
            ERSPANHardwareId=63
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
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
            [Tunnel]
            ERSPANHardwareId=<error descr="Invalid value">64</error>
            ERSPANHardwareId=<error descr="Invalid value">100</error>
            ERSPANHardwareId=<error descr="Invalid value">-1</error>
            ERSPANHardwareId=<error descr="Invalid value">abc</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }
}
