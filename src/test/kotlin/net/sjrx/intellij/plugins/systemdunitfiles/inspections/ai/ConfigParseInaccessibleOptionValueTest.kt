package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseInaccessibleOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // From TEST-13-NSPAWN; the value is taken verbatim, so spaces and colons are part of the path.
        // language="unit file (systemd)"
        val file = """
            [Files]
            Inaccessible=/opt/inaccessible
            Inaccessible=/var
            Inaccessible=/path with spaces
            Inaccessible=/a:b
            Inaccessible=/
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Only absolute paths are accepted.
        // language="unit file (systemd)"
        val file = """
            [Files]
            Inaccessible=opt/x
            Inaccessible=./x
            Inaccessible=~/x
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(3, highlights)
    }
}
