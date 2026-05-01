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
            Bridge=12eth
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(0, myFixture.doHighlighting())
    }

    @Test
    fun testInvalidInterfaceNames() {
        // language="unit file (systemd)"
        val file = """
            [Network]
            Bridge=bad/name
            Bridge=thisnameiswaytoolong
            Bridge=eth0:1
            Bridge=eth%d
            Bridge=all
            Bridge=default
            Bridge=1234
            Bridge=0x10
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        assertSize(8, myFixture.doHighlighting())
    }
}
