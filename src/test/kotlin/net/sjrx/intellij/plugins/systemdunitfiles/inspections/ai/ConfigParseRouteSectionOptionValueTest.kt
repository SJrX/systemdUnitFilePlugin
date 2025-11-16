package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseRouteSectionOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Route]
            Type=unicast
            Type=local
            Type=broadcast
            Type=anycast
            Type=multicast
            Type=blackhole
            Type=unreachable
            Type=prohibit
            Type=throw
            Type=nat
            Type=xresolve
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
            [Route]
            Type=<error descr="Invalid option value">invalid</error>
            Type=<error descr="Invalid option value">unknown</error>
            Type=<error descr="Invalid option value">UNICAST</error>
            Type=<error descr="Invalid option value">multi-cast</error>
            Type=<error descr="Invalid option value">drop</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }

    @Test
    fun testDefaultValue() {
        // Fixture Setup - testing that 'unicast' is the default (implicit valid value)
        // language="unit file (systemd)"
        val file = """
            [Route]
            Type=unicast
            Destination=192.168.1.0/24
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testSpecialRouteTypes() {
        // Fixture Setup - testing special route types
        // language="unit file (systemd)"
        val file = """
            [Route]
            Type=blackhole
            Type=unreachable
            Type=prohibit
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }
}
