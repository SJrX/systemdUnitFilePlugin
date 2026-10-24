package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseMacAddressPolicyOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Link]
            MACAddressPolicy=persistent
            MACAddressPolicy=random
            MACAddressPolicy=none
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Link]
            MACAddressPolicy=Persistent
            MACAddressPolicy=keep
            MACAddressPolicy=yes
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(3, highlights)
    }
}
