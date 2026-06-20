package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseUnsetEnvironOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language=unit file (systemd)
        val file = """
            [Service]
            UnsetEnvironment=FOO
            UnsetEnvironment=FOO BAR=baz
            UnsetEnvironment=_PRIVATE PATH=/usr/bin
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Names cannot start with a digit or contain '-'.
        // language=unit file (systemd)
        val file = """
            [Service]
            UnsetEnvironment=1BAD
            UnsetEnvironment=has-dash
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(2, highlights)
    }
}
