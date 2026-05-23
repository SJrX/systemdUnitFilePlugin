package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseBindNetworkInterfaceOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            BindNetworkInterface=eth0
            BindNetworkInterface=lo
            BindNetworkInterface=wlan0
            BindNetworkInterface=br-1234abcd
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
            BindNetworkInterface=<error descr="Invalid value">all</error>
            BindNetworkInterface=<error descr="Invalid value">default</error>
            BindNetworkInterface=<error descr="Invalid value">eth0 eth1</error>
            BindNetworkInterface=<error descr="Invalid value">eth0/0</error>
            BindNetworkInterface=<error descr="Invalid value">123</error>
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(5, highlights)
    }
}
