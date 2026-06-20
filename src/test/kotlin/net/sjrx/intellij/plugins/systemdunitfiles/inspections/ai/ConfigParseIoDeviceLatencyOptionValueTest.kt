package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseIoDeviceLatencyOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language=unit file (systemd)
        val file = """
            [Service]
            IODeviceLatencyTargetSec=/dev/sda 10ms
            IODeviceLatencyTargetSec=/dev/sdb 1s
            IODeviceLatencyTargetSec=/dev/sdc 0
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // A non-time-span latency, and a path with no latency, are invalid.
        // language=unit file (systemd)
        val file = """
            [Service]
            IODeviceLatencyTargetSec=/dev/sda notatime
            IODeviceLatencyTargetSec=/dev/sdb
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(2, highlights)
    }
}
