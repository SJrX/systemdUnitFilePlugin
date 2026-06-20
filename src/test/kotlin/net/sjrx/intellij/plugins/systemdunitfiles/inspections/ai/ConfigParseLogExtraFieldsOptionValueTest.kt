package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseLogExtraFieldsOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language=unit file (systemd)
        val file = """
            [Service]
            LogExtraFields=FIELD=value
            LogExtraFields=PRIORITY=6 MESSAGE_ID=abc
            LogExtraFields=EMPTY=
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Journal field names must be uppercase and each entry needs a '='.
        // language=unit file (systemd)
        val file = """
            [Service]
            LogExtraFields=lowercase=value
            LogExtraFields=NOEQUALS
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(2, highlights)
    }
}
