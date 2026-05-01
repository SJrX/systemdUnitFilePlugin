package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseFdnameOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Socket]
            FileDescriptorName=valid_name_1
            FileDescriptorName=valid-name-2
            FileDescriptorName=AnotherFd
            FileDescriptorName=fd.with.dots
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.socket", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Fixture Setup - both lines contain a ':' which fdname_is_valid rejects
        // language="unit file (systemd)"
        val file = """
            [Socket]
            FileDescriptorName=invalid:name_1
            FileDescriptorName=invalid:name_2
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.socket", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(2, highlights)
    }
}
