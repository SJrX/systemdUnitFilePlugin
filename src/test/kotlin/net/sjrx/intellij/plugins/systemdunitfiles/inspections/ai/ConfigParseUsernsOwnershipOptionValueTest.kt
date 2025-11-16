package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseUsernsOwnershipOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidEnumValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Files]
            PrivateUsersOwnership=off
            PrivateUsersOwnership=chown
            PrivateUsersOwnership=map
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidBooleanValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Files]
            PrivateUsersOwnership=yes
            PrivateUsersOwnership=no
            PrivateUsersOwnership=true
            PrivateUsersOwnership=false
            PrivateUsersOwnership=on
            PrivateUsersOwnership=1
            PrivateUsersOwnership=0
            PrivateUsersOwnership=y
            PrivateUsersOwnership=n
            PrivateUsersOwnership=t
            PrivateUsersOwnership=f
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.nspawn", file)
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
            [Files]
            PrivateUsersOwnership=invalid
            PrivateUsersOwnership=auto
            PrivateUsersOwnership=chmod
            PrivateUsersOwnership=own
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }
}
