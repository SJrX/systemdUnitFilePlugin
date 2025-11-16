package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseCodelU32OptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [ControlledDelay]
            PacketLimit=0
            PacketLimit=1
            PacketLimit=100
            PacketLimit=1000
            PacketLimit=4294967294
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
            [ControlledDelay]
            PacketLimit=-1
            PacketLimit=4294967295
            PacketLimit=5000000000
            PacketLimit=abc
            PacketLimit=12.5
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
            [ControlledDelay]
            PacketLimit=0
            PacketLimit=4294967294
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testJustOverMaximum() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [ControlledDelay]
            PacketLimit=4294967295
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }
}
