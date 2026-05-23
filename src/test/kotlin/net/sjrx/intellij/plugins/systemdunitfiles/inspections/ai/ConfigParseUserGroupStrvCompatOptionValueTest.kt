package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseUserGroupStrvCompatOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            SupplementaryGroups=wheel
            SupplementaryGroups=wheel adm
            SupplementaryGroups=users
            SupplementaryGroups=1000
            SupplementaryGroups=group-with-dash
            SupplementaryGroups=group_with_underscore
            SupplementaryGroups=%n
            SupplementaryGroups=app-%i wheel
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
            SupplementaryGroups=<error descr="Invalid value">.</error>
            SupplementaryGroups=<error descr="Invalid value">..</error>
            SupplementaryGroups=<error descr="Invalid value">group/with/slash</error>
            SupplementaryGroups=<error descr="Invalid value">group:with:colon</error>
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(4, highlights)
    }
}
