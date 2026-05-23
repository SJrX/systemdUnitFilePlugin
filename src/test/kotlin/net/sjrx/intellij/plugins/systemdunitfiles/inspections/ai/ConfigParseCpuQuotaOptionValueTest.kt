package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseCpuQuotaOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            CPUQuota=50%
            CPUQuota=100%
            CPUQuota=200%
            CPUQuota=12.34%
            CPUQuota=1.5%
            CPUQuota=0.01%
            CPUQuota=99.99%
            CPUQuota=1000%
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
            CPUQuota=<error descr="Invalid value">50</error>
            CPUQuota=<error descr="Invalid value">abc</error>
            CPUQuota=<error descr="Invalid value">-5%</error>
            CPUQuota=<error descr="Invalid value">12.345%</error>
            CPUQuota=<error descr="Invalid value">50% 60%</error>
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(5, highlights)
    }
}
