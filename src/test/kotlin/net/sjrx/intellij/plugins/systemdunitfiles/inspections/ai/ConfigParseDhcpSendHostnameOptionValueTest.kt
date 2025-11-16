package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDhcpSendHostnameOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidBooleanValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [DHCPv4]
            SendHostname=yes
            SendHostname=no
            SendHostname=true
            SendHostname=false
            SendHostname=on
            SendHostname=off
            SendHostname=1
            SendHostname=0
            SendHostname=y
            SendHostname=n
            SendHostname=t
            SendHostname=f
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [DHCPv4]
            SendHostname=invalid
            SendHostname=maybe
            SendHostname=2
            SendHostname=enabled
            SendHostname=disabled
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }

    @Test
    fun testBooleanValuesWithWhitespace() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [DHCPv4]
            SendHostname=yes 
            SendHostname=true  
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }
}
