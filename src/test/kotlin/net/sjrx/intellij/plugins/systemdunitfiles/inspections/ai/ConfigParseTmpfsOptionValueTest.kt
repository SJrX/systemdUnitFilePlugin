package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseTmpfsOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // From TEST-13-NSPAWN; the option string is passed to the kernel unchecked.
        // language="unit file (systemd)"
        val file = """
            [Files]
            TemporaryFileSystem=/tmp
            TemporaryFileSystem=/opt/tmp
            TemporaryFileSystem=/var:rw,nosuid,noexec
            TemporaryFileSystem=/tmp:
            TemporaryFileSystem=/tmp:mode=1777,size=10%
            TemporaryFileSystem=/path\:x:mode=0700
            TemporaryFileSystem=/
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // The path must be absolute.
        // language="unit file (systemd)"
        val file = """
            [Files]
            TemporaryFileSystem=tmp
            TemporaryFileSystem=:mode=0755
            TemporaryFileSystem=relative:size=1M
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(3, highlights)
    }
}
