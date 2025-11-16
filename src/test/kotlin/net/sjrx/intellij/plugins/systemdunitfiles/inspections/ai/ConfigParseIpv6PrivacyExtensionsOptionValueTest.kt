package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseIpv6PrivacyExtensionsOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidBooleanValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            IPv6PrivacyExtensions=yes
            IPv6PrivacyExtensions=no
            IPv6PrivacyExtensions=true
            IPv6PrivacyExtensions=false
            IPv6PrivacyExtensions=on
            IPv6PrivacyExtensions=off
            IPv6PrivacyExtensions=1
            IPv6PrivacyExtensions=0
            IPv6PrivacyExtensions=y
            IPv6PrivacyExtensions=n
            IPv6PrivacyExtensions=t
            IPv6PrivacyExtensions=f
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidSpecialValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Network]
            IPv6PrivacyExtensions=prefer-public
            IPv6PrivacyExtensions=kernel
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
            IPv6PrivacyExtensions=invalid
            IPv6PrivacyExtensions=maybe
            IPv6PrivacyExtensions=prefer-private
            IPv6PrivacyExtensions=2
            IPv6PrivacyExtensions=enabled
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }
}
