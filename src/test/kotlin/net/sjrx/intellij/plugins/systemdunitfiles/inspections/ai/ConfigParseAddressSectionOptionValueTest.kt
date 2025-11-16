package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseAddressSectionOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Address]
            RouteMetric=0
            RouteMetric=100
            RouteMetric=1000
            RouteMetric=4294967295
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
            [Address]
            RouteMetric=<error descr="Invalid value supplied for option.">4294967296</error>
            RouteMetric=<error descr="Invalid value supplied for option.">-1</error>
            RouteMetric=<error descr="Invalid value supplied for option.">invalid</error>
            RouteMetric=<error descr="Invalid value supplied for option.">1.5</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }
    
    @Test
    fun testBoundaryValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Address]
            RouteMetric=0
            RouteMetric=4294967295
            RouteMetric=<error descr="Invalid value supplied for option.">4294967296</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }
}
