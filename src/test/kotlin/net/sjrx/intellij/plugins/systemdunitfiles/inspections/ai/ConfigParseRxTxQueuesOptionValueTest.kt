package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseRxTxQueuesOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Link]
            ReceiveQueues=1
            ReceiveQueues=100
            ReceiveQueues=1024
            ReceiveQueues=4096
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
            ReceiveQueues=<error descr="Invalid value">0</error>
            ReceiveQueues=<error descr="Invalid value">4097</error>
            ReceiveQueues=<error descr="Invalid value">5000</error>
            ReceiveQueues=<error descr="Invalid value">-1</error>
            ReceiveQueues=<error descr="Invalid value">abc</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.link", file)
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
            [Link]
            ReceiveQueues=1
            ReceiveQueues=4096
            ReceiveQueues=<error descr="Invalid value">0</error>
            ReceiveQueues=<error descr="Invalid value">4097</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(2, highlights)
    }
}
