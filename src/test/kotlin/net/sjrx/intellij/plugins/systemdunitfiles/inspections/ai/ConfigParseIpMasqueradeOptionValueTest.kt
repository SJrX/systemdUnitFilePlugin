package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseIpMasqueradeOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidPrimaryValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            IPMasquerade=ipv4
            IPMasquerade=ipv6
            IPMasquerade=both
            IPMasquerade=no
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidDeprecatedBooleanValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            IPMasquerade=yes
            IPMasquerade=true
            IPMasquerade=on
            IPMasquerade=1
            IPMasquerade=y
            IPMasquerade=t
            IPMasquerade=false
            IPMasquerade=off
            IPMasquerade=0
            IPMasquerade=n
            IPMasquerade=f
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
            IPMasquerade=invalid
            IPMasquerade=maybe
            IPMasquerade=ipv4-only
            IPMasquerade=2
            IPMasquerade=all
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }
}
