package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParsePermilleOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [CAN]
            SamplePoint=0%
            SamplePoint=50%
            SamplePoint=87.5%
            SamplePoint=99%
            SamplePoint=99.9%
            SamplePoint=100%
            SamplePoint=100.0%
            DataSamplePoint=75%
            DataSamplePoint=12.3%
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [CAN]
            SamplePoint=<error descr="Invalid value">50</error>
            SamplePoint=<error descr="Invalid value">100.5%</error>
            SamplePoint=<error descr="Invalid value">101%</error>
            SamplePoint=<error descr="Invalid value">200%</error>
            SamplePoint=<error descr="Invalid value">abc</error>
            SamplePoint=<error descr="Invalid value">50.12%</error>
            SamplePoint=<error descr="Invalid value">-5%</error>
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(7, highlights)
    }
}
