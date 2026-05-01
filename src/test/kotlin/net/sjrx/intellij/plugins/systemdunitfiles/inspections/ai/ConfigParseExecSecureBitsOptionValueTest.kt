package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseExecSecureBitsOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Service]
            SecureBits=keep-caps
            SecureBits=keep-caps-locked
            SecureBits=no-setuid-fixup
            SecureBits=no-setuid-fixup-locked
            SecureBits=noroot
            SecureBits=noroot-locked
            SecureBits=keep-caps keep-caps-locked no-setuid-fixup no-setuid-fixup-locked noroot noroot-locked
            SecureBits=keep-caps noroot
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.service", file)
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
            [Service]
            SecureBits=invalid_value
            SecureBits=keep-caps invalid_value
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(2, highlights)
    }
}
