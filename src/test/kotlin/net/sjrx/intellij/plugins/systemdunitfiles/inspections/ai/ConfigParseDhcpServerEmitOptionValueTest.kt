package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDhcpServerEmitOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [DHCPServer]
            DNS=192.168.1.1
            NTP=192.168.1.1 192.168.1.2
            SIP=_server_address
            POP3=_server_address 8.8.8.8
            SMTP=8.8.4.4 1.1.1.1 _server_address
            LPR=10.0.0.1
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
            [DHCPServer]
            DNS=<error descr="Invalid value">not-an-ip</error>
            DNS=<error descr="Invalid value">192.168.1.256</error>
            NTP=<error descr="Invalid value">::1</error>
            SIP=<error descr="Invalid value">192.168.1.1, 192.168.1.2</error>
            LPR=<error descr="Invalid value">_other_address</error>
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(5, highlights)
    }
}
