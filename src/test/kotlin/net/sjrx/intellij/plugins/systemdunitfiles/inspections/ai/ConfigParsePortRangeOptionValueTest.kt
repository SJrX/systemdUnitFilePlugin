package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParsePortRangeOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [VXLAN]
            PortRange=1-65535
            PortRange=4789
            PortRange=0x100-0x200
            PortRange=32768-61000
            PortRange=1000- 2000
        """.trimIndent()

        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [VXLAN]
            PortRange=0-100
            PortRange=1-65536
            PortRange=100:200
            PortRange=-5
            PortRange=5-
            PortRange=1000 -2000
            PortRange=+0b1-2
            PortRange=-0b0-2
            PortRange=5--0
            PortRange=-0-5
        """.trimIndent()

        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(10, highlights)
    }
}
