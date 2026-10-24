package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseSocketBindtodeviceOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // eth0/lo/wlan0 are from the Debian corpus and systemd's tests.
        // language="unit file (systemd)"
        val file = """
            [Socket]
            BindToDevice=eth0
            BindToDevice=lo
            BindToDevice=wlan0
            BindToDevice=*
            BindToDevice=enp0s31f6
            BindToDevice=br-lan.100
            BindToDevice=abcdefghijklmno
            BindToDevice=%i
            BindToDevice=vrf-%i
            BindToDevice=1a
            BindToDevice=alliance
            BindToDevice=**
        """.trimIndent()

        setupFileInEditor("file.socket", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Socket]
            BindToDevice=abcdefghijklmnop
            BindToDevice=eth0:1
            BindToDevice=eth/0
            BindToDevice=all
            BindToDevice=default
            BindToDevice=42
            BindToDevice=..
            BindToDevice=eth0 eth1
            BindToDevice=%z
            BindToDevice=100%%
        """.trimIndent()

        setupFileInEditor("file.socket", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(10, myFixture.doHighlighting())
    }
}
