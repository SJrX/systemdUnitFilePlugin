package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseL2tpTunnelLocalAddressOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [L2TP]
            Local=192.168.30.100
            Local=2001:db8::1
            Local=auto
            Local=static
            Local=dynamic
            Local=auto@eth0
            Local=dynamic@test1
            Local=192.168.30.100@eth0
            Local=auto@@eth0
            Local=auto@
            Local=static@a-very-long-alternative-interface-name
            Local=@auto
            Local=@@192.168.30.100@eth0
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
            [L2TP]
            Local=any
            Local=auto@all
            Local=auto@eth/0
            Local=192.168.30.256
            Local=auto@123
            Local=@
            Local=Auto
            Local=STATIC@eth0
        """.trimIndent()

        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(8, highlights)
    }
}
