package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseAddressSectionAddressScopeOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidLiteralValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Address]
            Scope=global
            Scope=link
            Scope=host
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidIntegerValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Address]
            Scope=0
            Scope=127
            Scope=255
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidLiteralValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Address]
            Scope=invalid
            Scope=localhost
            Scope=site
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(3, highlights)
    }

    @Test
    fun testInvalidIntegerValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Address]
            Scope=256
            Scope=-1
            Scope=1000
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(3, highlights)
    }

    @Test
    fun testMixedInvalidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Address]
            Scope=global
            Scope=invalid_value
            Scope=100
            Scope=300
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(2, highlights)
    }
}
