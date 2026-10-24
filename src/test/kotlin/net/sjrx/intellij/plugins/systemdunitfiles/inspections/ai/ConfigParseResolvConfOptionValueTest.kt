package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseResolvConfOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Every resolv_conf_mode_table entry plus booleans (case-insensitive).
        // language="unit file (systemd)"
        val file = """
            [Exec]
            ResolvConf=off
            ResolvConf=copy-host
            ResolvConf=copy-static
            ResolvConf=copy-uplink
            ResolvConf=copy-stub
            ResolvConf=replace-host
            ResolvConf=replace-static
            ResolvConf=replace-uplink
            ResolvConf=replace-stub
            ResolvConf=bind-host
            ResolvConf=bind-static
            ResolvConf=bind-uplink
            ResolvConf=bind-stub
            ResolvConf=delete
            ResolvConf=auto
            ResolvConf=yes
            ResolvConf=no
            ResolvConf=True
            ResolvConf=OFF
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Not a table entry; table entries are case-sensitive.
        // language="unit file (systemd)"
        val file = """
            [Exec]
            ResolvConf=copy
            ResolvConf=Copy-Host
            ResolvConf=bind-hosts
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(3, highlights)
    }
}
