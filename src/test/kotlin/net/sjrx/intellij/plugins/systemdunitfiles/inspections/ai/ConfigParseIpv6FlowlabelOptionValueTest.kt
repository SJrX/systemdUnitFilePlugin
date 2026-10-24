package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseIpv6FlowlabelOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Tunnel]
            IPv6FlowLabel=inherit
            IPv6FlowLabel=0
            IPv6FlowLabel=1048575
            IPv6FlowLabel=0xFFFFF
            IPv6FlowLabel=0x12345
            IPv6FlowLabel=0b 1
            IPv6FlowLabel=0b-0
        """.trimIndent()

        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Tunnel]
            IPv6FlowLabel=1048576
            IPv6FlowLabel=0x100000
            IPv6FlowLabel=Inherit
            IPv6FlowLabel=-1
        """.trimIndent()

        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(4, highlights)
    }
}
