package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseServiceTimeoutOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Service]
            TimeoutSec=infinity
            TimeoutStartSec=5s
            TimeoutSec=10m
            TimeoutStartSec=30
            TimeoutSec=0
            TimeoutStartSec=500ms
            TimeoutSec=2h
            TimeoutStartSec=1min 30s
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.service", file)
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
            [Service]
            TimeoutStartSec=abc
            TimeoutSec=-10s
            TimeoutStartSec=INFINITY
            TimeoutSec=forever
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }
}
