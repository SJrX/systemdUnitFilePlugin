package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseVxlanAddressOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [VXLAN]
            Group=239.1.1.1
            Group=ff02::42
            Local=192.168.1.1
            Local=fe80::1
            Local=dhcp4
            Local=ipv6_link_local
            Local=slaac
            Local=dhcp_pd
            Remote=10.0.0.2
            Remote=2001:db8::1
            Remote=::ffff:10.0.0.2
            Group=FF05::2
            Group=224.0.0.1
            Group=239.255.255.255
            Remote=223.255.255.255
            Remote=fe80::1
            Local=::ffff:239.1.1.1
            Local=dhcp6
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
            Group=239.1.1
            Local=dhcp
            Remote=example.com
            Remote=10.0.0.256
            Local=192.168.1.1/24
            Group=fe80::1::2
            Group=dhcp4
            Remote=slaac
            Group=192.168.1.1
            Group=240.0.0.1
            Group=ff::1
            Group=::ffff:239.1.1.1
            Remote=239.1.1.1
            Remote=ff02::1
            Local=239.1.1.1
            Local=ff02::1
        """.trimIndent()

        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(16, highlights)
    }
}
