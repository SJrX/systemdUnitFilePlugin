package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseRpsCpuMaskOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Link]
            ReceivePacketSteeringCPUMask=0 1
            ReceivePacketSteeringCPUMask=2,3
            ReceivePacketSteeringCPUMask=all
            ReceivePacketSteeringCPUMask=disable
            ReceivePacketSteeringCPUMask=0-7
            ReceivePacketSteeringCPUMask=1
            ReceivePacketSteeringCPUMask=0-"3"
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Link]
            ReceivePacketSteeringCPUMask=invalid
            ReceivePacketSteeringCPUMask=0 3 8-invalid
            ReceivePacketSteeringCPUMask=all 1
            ReceivePacketSteeringCPUMask=none
            ReceivePacketSteeringCPUMask=-0
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(5, highlights)
    }
}
