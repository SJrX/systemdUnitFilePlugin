package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseErspanIndexOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Tunnel]
            ERSPANIndex=0
            ERSPANIndex=1
            ERSPANIndex=100
            ERSPANIndex=1048575
            ERSPANIndex=500000
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
            ERSPANIndex=<error descr="Invalid value">1048576</error>
            ERSPANIndex=<error descr="Invalid value">2000000</error>
            ERSPANIndex=<error descr="Invalid value">-1</error>
            ERSPANIndex=<error descr="Invalid value">invalid</error>
            ERSPANIndex=<error descr="Invalid value">12.5</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }

    @Test
    fun testBoundaryValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Tunnel]
            ERSPANIndex=0
            ERSPANIndex=1048575
            ERSPANIndex=<error descr="Invalid value">1048576</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }
}
