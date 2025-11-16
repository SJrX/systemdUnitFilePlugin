package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseKeepConfigurationOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidEnumValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            KeepConfiguration=no
            KeepConfiguration=static
            KeepConfiguration=dynamic-on-stop
            KeepConfiguration=dynamic
            KeepConfiguration=yes
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
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
            [Network]
            KeepConfiguration=1
            KeepConfiguration=yes
            KeepConfiguration=y
            KeepConfiguration=true
            KeepConfiguration=t
            KeepConfiguration=on
            KeepConfiguration=0
            KeepConfiguration=n
            KeepConfiguration=false
            KeepConfiguration=f
            KeepConfiguration=off
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidBackwardCompatibilityValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            KeepConfiguration=dhcp
            KeepConfiguration=dhcp-on-stop
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
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
            [Network]
            KeepConfiguration=invalid
            KeepConfiguration=maybe
            KeepConfiguration=always
            KeepConfiguration=never
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }
}
