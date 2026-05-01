package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseCoalesceSecOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
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
            RxCoalesceSec=1.5s
            TxCoalesceSec=1min 30sec
            RxCoalesceSec=infinity
            TxCoalesceSec=2hour
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Link]
            RxCoalesceSec=-1
            TxCoalesceSec=abc
            TxCoalesceIrqSec=5z
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(3, myFixture.doHighlighting())
    }
}
