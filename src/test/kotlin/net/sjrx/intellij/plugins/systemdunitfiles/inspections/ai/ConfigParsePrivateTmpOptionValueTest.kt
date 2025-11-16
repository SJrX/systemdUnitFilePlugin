package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParsePrivateTmpOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidBooleanTrueValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            PrivateTmp=yes
            PrivateTmp=true
            PrivateTmp=1
            PrivateTmp=on
            PrivateTmp=t
            PrivateTmp=y
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
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
            [Swap]
            PrivateTmp=no
            PrivateTmp=false
            PrivateTmp=0
            PrivateTmp=off
            PrivateTmp=f
            PrivateTmp=n
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidDisconnectedValue() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            PrivateTmp=disconnected
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
            PrivateTmp=invalid
            PrivateTmp=maybe
            PrivateTmp=2
            PrivateTmp=disconnect
            PrivateTmp=enabled
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }
}
