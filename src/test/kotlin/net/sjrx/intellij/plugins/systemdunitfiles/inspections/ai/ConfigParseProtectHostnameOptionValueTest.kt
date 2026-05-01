package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseProtectHostnameOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidBooleanValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            ProtectHostname=yes
            ProtectHostname=no
            ProtectHostname=true
            ProtectHostname=false
            ProtectHostname=on
            ProtectHostname=off
            ProtectHostname=1
            ProtectHostname=0
            ProtectHostname=private
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()
        assertSize(0, highlights)
    }

    @Test
    fun testValidKeywordWithHostname() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            ProtectHostname=yes:my-host
            ProtectHostname=private:example.com
            ProtectHostname=true:foo.bar.example
            ProtectHostname=on:host1
            ProtectHostname=1:a-b-c.d
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
            ProtectHostname=invalid
            ProtectHostname=yes:-bad-leading-hyphen
            ProtectHostname=private:bad..double.dot
            ProtectHostname=yes:bad_underscore
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()
        assertSize(4, highlights)
    }
}
