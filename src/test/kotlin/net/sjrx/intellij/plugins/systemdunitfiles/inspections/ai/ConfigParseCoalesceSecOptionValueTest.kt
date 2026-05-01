package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseCoalesceSecOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Link]
            RxCoalesceSec=0
            RxCoalesceIrqSec=1
            TxCoalesceSec=60
            TxCoalesceIrqSec=500ms
            StatisticsBlockCoalesceSec=2s
            RxCoalesceLowSec=10us
            TxCoalesceLowSec=100µs
            RxCoalesceHighSec=1m
            TxCoalesceHighSec=1h
            CoalescePacketRateSampleIntervalSec=1
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
            RxCoalesceSec=<error descr="Invalid value">-1</error>
            TxCoalesceSec=<error descr="Invalid value">abc</error>
            RxCoalesceIrqSec=<error descr="Invalid value">1.5s</error>
            TxCoalesceIrqSec=<error descr="Invalid value">5z</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }
}
