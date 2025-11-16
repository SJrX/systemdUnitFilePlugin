package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParsePfifoSizeOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [PFIFOHeadDrop]
            PacketLimit=0
            PacketLimit=1
            PacketLimit=100
            PacketLimit=1000
            PacketLimit=65535
            PacketLimit=4294967295
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
            [PFIFOHeadDrop]
            PacketLimit=-1
            PacketLimit=4294967296
            PacketLimit=abc
            PacketLimit=10.5
            PacketLimit=0x100
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
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
            [PFIFOHeadDrop]
            PacketLimit=0
            PacketLimit=4294967295
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }
}
