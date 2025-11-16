package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseMtuOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            IPv6MTUBytes=1280
            IPv6MTUBytes=1500
            IPv6MTUBytes=9000
            IPv6MTUBytes=65536
            IPv6MTUBytes=4294967295
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValuesBelowMinimum() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            IPv6MTUBytes=0
            IPv6MTUBytes=1279
            IPv6MTUBytes=576
            IPv6MTUBytes=1000
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }

    @Test
    fun testInvalidValuesAboveMaximum() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            IPv6MTUBytes=4294967296
            IPv6MTUBytes=5000000000
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(2, highlights)
    }

    @Test
    fun testInvalidNonNumericValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            IPv6MTUBytes=abc
            IPv6MTUBytes=12.5
            IPv6MTUBytes=-1500
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(3, highlights)
    }
}
