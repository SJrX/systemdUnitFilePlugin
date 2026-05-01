package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseJobRunningTimeoutSecOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Unit]
            JobRunningTimeoutSec=10
            JobRunningTimeoutSec=10s
            JobRunningTimeoutSec=100ms
            JobRunningTimeoutSec=1m
            JobRunningTimeoutSec=1h
            JobRunningTimeoutSec=1d
            JobRunningTimeoutSec=infinity
            JobRunningTimeoutSec=10 s
            JobRunningTimeoutSec=1.5h
            JobRunningTimeoutSec=1min 30s
            JobRunningTimeoutSec=2hour
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Unit]
            JobRunningTimeoutSec=invalid
            JobRunningTimeoutSec=10x
            JobRunningTimeoutSec=10.5.2s
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(3, myFixture.doHighlighting())
    }
}
