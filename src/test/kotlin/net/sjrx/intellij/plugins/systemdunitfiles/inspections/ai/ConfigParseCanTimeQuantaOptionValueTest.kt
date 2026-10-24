package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseCanTimeQuantaOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [CAN]
            TimeQuantaNSec=100
            TimeQuantaNSec=500ns
            TimeQuantaNSec=1us
            TimeQuantaNSec=1ms
            DataTimeQuantaNSec=1ms 500us
            DataTimeQuantaNSec=10.5ms
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [CAN]
            TimeQuantaNSec=invalid
            TimeQuantaNSec=-10ns
            # config_parse_can_time_quanta requires 1…UINT32_MAX ns (sd-check: "Time quanta out of permitted
            # range"); infinity was asserted valid before #568.
            DataTimeQuantaNSec=infinity
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(3, myFixture.doHighlighting())
    }
}
