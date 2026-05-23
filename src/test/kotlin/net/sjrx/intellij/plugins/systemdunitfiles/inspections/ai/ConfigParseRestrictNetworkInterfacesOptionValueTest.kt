package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseRestrictNetworkInterfacesOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            RestrictNetworkInterfaces=eth0
            RestrictNetworkInterfaces=eth0 eth1
            RestrictNetworkInterfaces=lo eth0 wlan0
            RestrictNetworkInterfaces=~eth0
            RestrictNetworkInterfaces=~lo eth0
            RestrictNetworkInterfaces=br-1234abcd
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
            RestrictNetworkInterfaces=<error descr="Invalid value">all</error>
            RestrictNetworkInterfaces=<error descr="Invalid value">default</error>
            RestrictNetworkInterfaces=<error descr="Invalid value">eth0/0</error>
            RestrictNetworkInterfaces=<error descr="Invalid value">eth0:0</error>
            RestrictNetworkInterfaces=<error descr="Invalid value">123</error>
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(5, highlights)
    }
}
