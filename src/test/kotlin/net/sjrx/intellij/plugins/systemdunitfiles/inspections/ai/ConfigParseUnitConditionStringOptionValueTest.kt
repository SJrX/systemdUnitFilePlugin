package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseUnitConditionStringOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidBooleanValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Unit]
            ConditionFirstBoot=yes
            ConditionFirstBoot=no
            ConditionFirstBoot=true
            ConditionFirstBoot=false
            ConditionFirstBoot=1
            ConditionFirstBoot=0
            ConditionFirstBoot=on
            ConditionFirstBoot=off
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidWithNegatePrefix() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Unit]
            ConditionFirstBoot=!yes
            ConditionFirstBoot=!no
            ConditionFirstBoot=! yes
            ConditionFirstBoot=!  no
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidWithTriggerPrefix() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Unit]
            ConditionFirstBoot=|yes
            ConditionFirstBoot=|no
            ConditionFirstBoot=| yes
            ConditionFirstBoot=|  no
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidWithBothPrefixes() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Unit]
            ConditionFirstBoot=|!yes
            ConditionFirstBoot=| !no
            ConditionFirstBoot=|  !  yes
            ConditionFirstBoot=| ! no
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidNonBooleanValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Unit]
            ConditionFirstBoot=<error descr="Invalid option value">maybe</error>
            ConditionFirstBoot=<error descr="Invalid option value">2</error>
            ConditionFirstBoot=<error descr="Invalid option value">invalid</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(3, highlights)
    }

    @Test
    fun testInvalidPrefixWithoutValue() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Unit]
            ConditionFirstBoot=<error descr="Invalid option value">|</error>
            ConditionFirstBoot=<error descr="Invalid option value">!</error>
            ConditionFirstBoot=<error descr="Invalid option value">|!</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(3, highlights)
    }

    @Test
    fun testInvalidReversedPrefixes() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Unit]
            ConditionFirstBoot=<error descr="Invalid option value">!|yes</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }
}
