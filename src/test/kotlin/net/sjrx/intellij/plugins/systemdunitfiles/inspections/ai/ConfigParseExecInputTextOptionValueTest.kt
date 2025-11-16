package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseExecInputTextOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidArbitraryText() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            StandardInputText=Hello World
            StandardInputText=Some text with spaces
            StandardInputText=Text with numbers 12345
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidTextWithEscapes() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            StandardInputText=Line with\nnewline
            StandardInputText=Tab\there
            StandardInputText=Quote\"test
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidTextWithSpecifiers() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            StandardInputText=%n
            StandardInputText=%p-%i
            StandardInputText=Unit: %N
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidSpecialCharacters() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            StandardInputText=!@#$%^&*()
            StandardInputText=[]{};:',.<>?/
            StandardInputText=Mixed-123_ABC
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidSingleCharacter() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            StandardInputText=A
            StandardInputText=1
            StandardInputText=.
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidLongText() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            StandardInputText=This is a very long line of text that contains many words and could span quite far across the configuration file but should still be valid
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }
}
