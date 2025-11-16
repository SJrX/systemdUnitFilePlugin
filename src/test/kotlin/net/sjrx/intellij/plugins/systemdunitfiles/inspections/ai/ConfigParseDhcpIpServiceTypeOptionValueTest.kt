package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDhcpIpServiceTypeOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [DHCPv4]
            IPServiceType=none
            IPServiceType=CS6
            IPServiceType=CS4
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
            IPServiceType=<error descr="Invalid value supplied for option.">CS0</error>
            IPServiceType=<error descr="Invalid value supplied for option.">cs6</error>
            IPServiceType=<error descr="Invalid value supplied for option.">CS7</error>
            IPServiceType=<error descr="Invalid value supplied for option.">default</error>
            IPServiceType=<error descr="Invalid value supplied for option.">0</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }
}
