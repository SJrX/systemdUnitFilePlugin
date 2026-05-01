package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseIfnameOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidInterfaceNames() {
        // language="unit file (systemd)"
        val file = """
            [Network]
            Bridge=br0
            Bridge=eth0
            Bridge=my-iface
            Bridge=iface_1
            Bridge=abcdefghijklmno
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidInterfaceNames() {
        // Names containing '/' are rejected, and names longer than 15 characters
        // are rejected by ifname_valid().
        // language="unit file (systemd)"
        val file = """
            [Network]
            Bridge=bad/name
            Bridge=thisnameiswaytoolong
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()
        assertSize(2, highlights)
    }
}
