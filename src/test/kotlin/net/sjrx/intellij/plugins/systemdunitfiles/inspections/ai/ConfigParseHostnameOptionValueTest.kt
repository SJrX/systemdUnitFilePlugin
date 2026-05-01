package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseHostnameOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidHostnames() {
        // language="unit file (systemd)"
        val file = """
            [DHCPv4]
            Hostname=myhost
            Hostname=my-host
            Hostname=host.example.com
            Hostname=host123
            Hostname=123host
            Hostname=h
            Hostname=a-b-c
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidHostnames() {
        // language="unit file (systemd)"
        val file = """
            [DHCPv4]
            Hostname=-host
            Hostname=host-
            Hostname=host..com
            Hostname=host_name
            Hostname=host!
            Hostname=host name
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()
        assertSize(6, highlights)
    }
}
