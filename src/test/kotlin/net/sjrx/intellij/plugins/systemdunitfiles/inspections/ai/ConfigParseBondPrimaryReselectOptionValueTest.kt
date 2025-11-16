package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseBondPrimaryReselectOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Bond]
            PrimaryReselectPolicy=always
            PrimaryReselectPolicy=better
            PrimaryReselectPolicy=failure
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
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
            [Bond]
            PrimaryReselectPolicy=<error descr="Value 'never' is not valid">never</error>
            PrimaryReselectPolicy=<error descr="Value 'worst' is not valid">worst</error>
            PrimaryReselectPolicy=<error descr="Value 'random' is not valid">random</error>
            PrimaryReselectPolicy=<error descr="Value 'immediate' is not valid">immediate</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }
}
