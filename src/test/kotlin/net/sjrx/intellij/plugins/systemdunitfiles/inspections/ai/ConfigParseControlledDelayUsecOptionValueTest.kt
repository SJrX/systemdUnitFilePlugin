package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseControlledDelayUsecOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [ControlledDelay]
            TargetSec=10s
            IntervalSec=100ms
            CEThresholdSec=5
            TargetSec=1m
            IntervalSec=2h
            CEThresholdSec=500us
            TargetSec=10.5s
            IntervalSec=infinity
            CEThresholdSec=1min 30s
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [ControlledDelay]
            TargetSec=invalid
            IntervalSec=-10s
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(2, myFixture.doHighlighting())
    }
}
