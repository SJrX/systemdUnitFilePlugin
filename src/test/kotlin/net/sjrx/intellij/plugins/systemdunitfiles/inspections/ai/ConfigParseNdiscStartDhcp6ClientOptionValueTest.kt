package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseNdiscStartDhcp6ClientOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidBooleanValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [IPv6AcceptRA]
            DHCPv6Client=yes
            DHCPv6Client=no
            DHCPv6Client=true
            DHCPv6Client=false
            DHCPv6Client=on
            DHCPv6Client=off
            DHCPv6Client=1
            DHCPv6Client=0
            DHCPv6Client=y
            DHCPv6Client=n
            DHCPv6Client=t
            DHCPv6Client=f
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidSpecialValue() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [IPv6AcceptRA]
            DHCPv6Client=always
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
            [IPv6AcceptRA]
            DHCPv6Client=never
            DHCPv6Client=sometimes
            DHCPv6Client=maybe
            DHCPv6Client=2
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }
}
