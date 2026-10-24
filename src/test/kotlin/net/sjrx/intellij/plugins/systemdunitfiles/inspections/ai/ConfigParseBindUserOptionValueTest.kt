package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseBindUserOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // valid_user_group_name in strict mode: up to 31 characters.
        // language="unit file (systemd)"
        val file = """
            [Files]
            BindUser=
            BindUser=testuser
            BindUser=root
            BindUser=alice bob _svc
            BindUser=user-name_1
            BindUser=a234567890123456789012345678901
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Leading digit or dash, a dot, 32 characters, and a numeric UID are all refused by the strict check.
        // language="unit file (systemd)"
        val file = """
            [Files]
            BindUser=1user
            BindUser=john.doe
            BindUser=a2345678901234567890123456789012
            BindUser=-dash
            BindUser=1000
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(5, highlights)
    }
}
