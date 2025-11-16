package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseUsernsChownOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidBooleanTrueValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Files]
            PrivateUsersChown=1
            PrivateUsersChown=yes
            PrivateUsersChown=y
            PrivateUsersChown=true
            PrivateUsersChown=t
            PrivateUsersChown=on
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidBooleanFalseValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Files]
            PrivateUsersChown=0
            PrivateUsersChown=no
            PrivateUsersChown=n
            PrivateUsersChown=false
            PrivateUsersChown=f
            PrivateUsersChown=off
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
            PrivateUsersChown=<error descr="Invalid option value">maybe</error>
            PrivateUsersChown=<error descr="Invalid option value">2</error>
            PrivateUsersChown=<error descr="Invalid option value">enabled</error>
            PrivateUsersChown=<error descr="Invalid option value">disabled</error>
            PrivateUsersChown=<error descr="Invalid option value">chown</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }
}
