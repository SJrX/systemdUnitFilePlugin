package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseWolPasswordOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Link]
            WakeOnLanPassword=00:11:22:33:44:55
            WakeOnLanPassword=00-11-22-33-44-55
            WakeOnLanPassword=0011.2233.4455
            WakeOnLanPassword=/etc/systemd/network/wol.pass
            WakeOnLanPassword=/run/credentials/wol pass
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Link]
            WakeOnLanPassword=00:11:22:33:44
            WakeOnLanPassword=secret
            WakeOnLanPassword=/etc/../wol.pass
            WakeOnLanPassword=00:11:22:33:44:55:66:77
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(4, highlights)
    }
}
