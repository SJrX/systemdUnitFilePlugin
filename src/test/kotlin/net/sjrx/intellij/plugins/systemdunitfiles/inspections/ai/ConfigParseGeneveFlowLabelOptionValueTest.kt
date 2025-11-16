package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseGeneveFlowLabelOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [NetDev]
            Kind=geneve
            
            [GENEVE]
            FlowLabel=0
            FlowLabel=1
            FlowLabel=100
            FlowLabel=1048575
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("test.netdev", file)
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
            [NetDev]
            Kind=geneve
            
            [GENEVE]
            FlowLabel=<error descr="Invalid value">1048576</error>
            FlowLabel=<error descr="Invalid value">2000000</error>
            FlowLabel=<error descr="Invalid value">-1</error>
            FlowLabel=<error descr="Invalid value">invalid</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("test.netdev", file)
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
            [NetDev]
            Kind=geneve
            
            [GENEVE]
            FlowLabel=0
            FlowLabel=1048575
            FlowLabel=<error descr="Invalid value">1048576</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("test.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }
}
