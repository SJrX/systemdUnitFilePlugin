package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseNetworkZoneOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // `vz-` + name must be a valid ifname: up to 12 characters; digits-only is fine once prefixed.
        // language="unit file (systemd)"
        val file = """
            [Network]
            Zone=sd-zone0
            Zone=foo
            Zone=abcdefghijkl
            Zone=123
            Zone=a.b
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // 13 characters is too long; `/`, space and `%` are not ifname characters.
        // language="unit file (systemd)"
        val file = """
            [Network]
            Zone=abcdefghijklm
            Zone=foo/bar
            Zone=my zone
            Zone=a%d
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(4, highlights)
    }
}
