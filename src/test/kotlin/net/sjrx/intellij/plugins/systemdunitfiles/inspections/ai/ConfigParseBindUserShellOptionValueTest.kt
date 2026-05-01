package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseBindUserShellOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidBooleans() {
        // language="unit file (systemd)"
        val file = """
            [Files]
            BindUserShell=yes
            BindUserShell=no
            BindUserShell=true
            BindUserShell=false
            BindUserShell=on
            BindUserShell=off
            BindUserShell=1
            BindUserShell=0
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()
        assertSize(0, highlights)
    }

    @Test
    fun testValidAbsolutePaths() {
        // language="unit file (systemd)"
        val file = """
            [Files]
            BindUserShell=/bin/sh
            BindUserShell=/usr/bin/zsh
            BindUserShell=/bin/bash
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Files]
            BindUserShell=invalid
            BindUserShell=relative/path
            BindUserShell=bin/sh
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()
        assertSize(3, highlights)
    }
}
