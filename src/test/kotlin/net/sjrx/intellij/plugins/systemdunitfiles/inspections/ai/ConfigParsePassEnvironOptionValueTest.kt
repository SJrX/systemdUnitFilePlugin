package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParsePassEnvironOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            PassEnvironment=PATH
            PassEnvironment=HOME LANG TERM
            PassEnvironment=MY_VAR
            PassEnvironment=_LEADING_UNDERSCORE
            PassEnvironment=A B C D E
            PassEnvironment=%n_LOG_LEVEL
            PassEnvironment=PREFIX_%i
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            PassEnvironment=<error descr="Invalid value">1STARTS_WITH_DIGIT</error>
            PassEnvironment=<error descr="Invalid value">HAS-DASH</error>
            PassEnvironment=<error descr="Invalid value">HAS.DOT</error>
            PassEnvironment=<error descr="Invalid value">NAME=value</error>
            PassEnvironment=<error descr="Invalid value">PATH,HOME</error>
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(5, highlights)
    }
}
