package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseNsecOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            TimerSlackNSec=infinity
            TimerSlackNSec=50
            TimerSlackNSec=100us
            TimerSlackNSec=1ms
            TimerSlackNSec=10s
            TimerSlackNSec=1min
            TimerSlackNSec=2h
            TimerSlackNSec=1d
            TimerSlackNSec=1s 500ms
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            TimerSlackNSec=<error descr="Invalid value">abc</error>
            TimerSlackNSec=<error descr="Invalid value">-1</error>
            TimerSlackNSec=<error descr="Invalid value">10zz</error>
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(3, highlights)
    }
}
