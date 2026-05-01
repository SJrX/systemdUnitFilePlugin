package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseUdevPropertyNameOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidSingleProperty() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Link]
            UnsetProperty=ID_NET_NAME
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidMultipleProperties() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Link]
            UnsetProperty=ID_NET_NAME ID_NET_NAME_PATH ID_NET_NAME_SLOT
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidLowercaseAndUnderscoreLeading() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Link]
            ImportProperty=lowercase_ok _leading_underscore MIXED_Case123
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidLeadingDigit() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Link]
            UnsetProperty=1INVALID
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }

    @Test
    fun testInvalidContainsHyphen() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Link]
            UnsetProperty=ID-NET-NAME
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }

    @Test
    fun testInvalidContainsDot() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Link]
            ImportProperty=ID.NET.NAME
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }
}
