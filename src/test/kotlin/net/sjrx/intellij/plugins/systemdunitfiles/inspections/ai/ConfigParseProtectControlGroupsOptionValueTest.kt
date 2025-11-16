package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseProtectControlGroupsOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidBooleanValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            ProtectControlGroups=true
            ProtectControlGroups=false
            ProtectControlGroups=yes
            ProtectControlGroups=no
            ProtectControlGroups=1
            ProtectControlGroups=0
            ProtectControlGroups=on
            ProtectControlGroups=off
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidSpecialValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            ProtectControlGroups=private
            ProtectControlGroups=strict
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            ProtectControlGroups=invalid
            ProtectControlGroups=2
            ProtectControlGroups=public
            ProtectControlGroups=enabled
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }

    @Test
    fun testValidShortBooleanValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            ProtectControlGroups=y
            ProtectControlGroups=n
            ProtectControlGroups=t
            ProtectControlGroups=f
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }
}
