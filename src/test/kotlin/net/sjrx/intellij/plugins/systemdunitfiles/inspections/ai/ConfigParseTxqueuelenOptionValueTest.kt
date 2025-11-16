package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseTxqueuelenOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Link]
            TransmitQueueLength=0
            TransmitQueueLength=1
            TransmitQueueLength=100
            TransmitQueueLength=1000
            TransmitQueueLength=4294967294
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
            TransmitQueueLength=<error descr="Cannot parse option value">4294967295</error>
            TransmitQueueLength=<error descr="Cannot parse option value">4294967296</error>
            TransmitQueueLength=<error descr="Cannot parse option value">-1</error>
            TransmitQueueLength=<error descr="Cannot parse option value">abc</error>
            TransmitQueueLength=<error descr="Cannot parse option value">1.5</error>
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
            TransmitQueueLength=4294967294
            TransmitQueueLength=<error descr="Cannot parse option value">4294967295</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }
}
