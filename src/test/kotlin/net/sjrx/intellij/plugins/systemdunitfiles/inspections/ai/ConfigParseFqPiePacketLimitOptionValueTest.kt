package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseFqPiePacketLimitOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [FlowQueuePIE]
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
            [FlowQueuePIE]
            PacketLimit=<error descr="Invalid value">0</error>
            PacketLimit=<error descr="Invalid value">-1</error>
            PacketLimit=<error descr="Invalid value">4294967295</error>
            PacketLimit=<error descr="Invalid value">4294967296</error>
            PacketLimit=<error descr="Invalid value">abc</error>
            PacketLimit=<error descr="Invalid value">123.45</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(6, highlights)
    }
}
