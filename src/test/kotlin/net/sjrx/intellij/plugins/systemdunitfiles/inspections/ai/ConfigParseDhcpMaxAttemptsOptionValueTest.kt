package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDhcpMaxAttemptsOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [DHCPv4]
            MaxAttempts=infinity
            MaxAttempts=1
            MaxAttempts=5
            MaxAttempts=100
            MaxAttempts=999999999999
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
            [DHCPv4]
            MaxAttempts=<error descr="Invalid value">0</error>
            MaxAttempts=<error descr="Invalid value">-1</error>
            MaxAttempts=<error descr="Invalid value">-5</error>
            MaxAttempts=<error descr="Invalid value">infinite</error>
            MaxAttempts=<error descr="Invalid value">INFINITY</error>
            MaxAttempts=<error descr="Invalid value">foo</error>
            MaxAttempts=<error descr="Invalid value">1.5</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(7, highlights)
    }
}
