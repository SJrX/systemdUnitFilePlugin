package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseWireguardRoutePriorityOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [WireGuard]
            RouteMetric=0
            RouteMetric=1024
            RouteMetric=100
            RouteMetric=4294967295
            RouteMetric=2147483647
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
            [WireGuard]
            RouteMetric=<error descr="Semantic error: Invalid value">-1</error>
            RouteMetric=<error descr="Semantic error: Invalid value">4294967296</error>
            RouteMetric=<error descr="Semantic error: Invalid value">not_a_number</error>
            RouteMetric=<error descr="Semantic error: Invalid value">10.5</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }
}
