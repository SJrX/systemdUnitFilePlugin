package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseVethExtraOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // From TEST-13-NSPAWN. An invalid container-side name is silently replaced by the host-side one, so it is accepted.
        // language="unit file (systemd)"
        val file = """
            [Network]
            VirtualEthernetExtra=my-fancy-veth1
            VirtualEthernetExtra=fancy-veth2:my-fancy-veth2
            VirtualEthernetExtra=ve-host:
            VirtualEthernetExtra=ve-host:name-that-is-far-too-long
            VirtualEthernetExtra=ve-a:all
            VirtualEthernetExtra=a:b\:c
            VirtualEthernetExtra=ve-a:0x10
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Empty/too long/reserved/ifindex-like host-side name, anything after a second colon (even empty), whitespace in
        // the name.
        // language="unit file (systemd)"
        val file = """
            [Network]
            VirtualEthernetExtra=:ve-b
            VirtualEthernetExtra=a:b:c
            VirtualEthernetExtra=a:b:
            VirtualEthernetExtra=averyveryverylongname
            VirtualEthernetExtra=default
            VirtualEthernetExtra=host name:x
            VirtualEthernetExtra=0x10:ve-b
            VirtualEthernetExtra=a\ b:c
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(8, highlights)
    }
}
