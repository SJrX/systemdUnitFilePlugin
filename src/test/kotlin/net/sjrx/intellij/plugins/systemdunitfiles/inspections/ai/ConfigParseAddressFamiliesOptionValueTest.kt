package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseAddressFamiliesOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            RestrictAddressFamilies=none
            RestrictAddressFamilies=AF_INET
            RestrictAddressFamilies=AF_INET AF_INET6
            RestrictAddressFamilies=AF_UNIX AF_NETLINK
            RestrictAddressFamilies=~AF_PACKET
            RestrictAddressFamilies=~AF_INET AF_INET6
            RestrictAddressFamilies=AF_BRIDGE AF_X25 AF_AX25
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
            RestrictAddressFamilies=<error descr="Invalid value">inet</error>
            RestrictAddressFamilies=<error descr="Invalid value">AF_inet</error>
            RestrictAddressFamilies=<error descr="Invalid value">AF_INET, AF_INET6</error>
            RestrictAddressFamilies=<error descr="Invalid value">~ AF_PACKET</error>
            RestrictAddressFamilies=<error descr="Invalid value">NONE</error>
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(5, highlights)
    }
}
