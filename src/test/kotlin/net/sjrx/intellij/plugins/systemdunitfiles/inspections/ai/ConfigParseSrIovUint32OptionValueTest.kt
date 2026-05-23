package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseSrIovUint32OptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [SR-IOV]
            VirtualFunction=0
            VirtualFunction=42
            VLANId=1
            VLANId=4095
            QualityOfService=0
            QualityOfService=4294967295
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [SR-IOV]
            VirtualFunction=<error descr="Invalid value">-1</error>
            VLANId=<error descr="Invalid value">abc</error>
            QualityOfService=<error descr="Invalid value">4294967296</error>
            VirtualFunction=<error descr="Invalid value">1.5</error>
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(4, highlights)
    }
}
