package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseSocketBindtodeviceOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // eth0/lo/wlan0 are from the Debian corpus and systemd's tests. The numeric-looking names after `**`
        // are ones parse_ifindex() does not take as an index, so systemd accepts them (sd-check at 0f0682b773).
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
            BindToDevice=-5
            BindToDevice=+0
            BindToDevice=0x0
            BindToDevice=+0b1
            BindToDevice=0x+5
            BindToDevice=0x80000000
            BindToDevice=+2147483648
            BindToDevice=0b2
            BindToDevice=0xg
            BindToDevice=00x5
        """.trimIndent()

        setupFileInEditor("file.socket", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testInvalidValues() {
        // 0x5 … 0x7fffffff: parse_ifindex() reads them as an ifindex, which ifname_valid() refuses
        // ("Invalid interface name, ignoring", sd-check at 0f0682b773).
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
            BindToDevice=0x5
            BindToDevice=0X5
            BindToDevice=+5
            BindToDevice=+0x5
            BindToDevice=0b101
            BindToDevice=0o7
            BindToDevice=0b+1
            BindToDevice=010
            BindToDevice=+010
            BindToDevice=0x7fffffff
        """.trimIndent()

        setupFileInEditor("file.socket", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(20, myFixture.doHighlighting())
    }
}
