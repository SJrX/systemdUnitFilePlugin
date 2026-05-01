package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseRequiredFamilyForOnlineOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Link]
            RequiredFamilyForOnline=ipv4
            RequiredFamilyForOnline=ipv6
            RequiredFamilyForOnline=both
            RequiredFamilyForOnline=any
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Link]
            RequiredFamilyForOnline=invalid
            RequiredFamilyForOnline=value
        """.trimIndent()

        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(2, highlights)
    }
}
