package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDfOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidBooleanValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [VXLAN]
            IPDoNotFragment=yes
            IPDoNotFragment=no
            IPDoNotFragment=1
            IPDoNotFragment=0
            IPDoNotFragment=true
            IPDoNotFragment=false
            IPDoNotFragment=on
            IPDoNotFragment=off
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidInheritValue() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [VXLAN]
            IPDoNotFragment=inherit
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
            [VXLAN]
            IPDoNotFragment=invalid
            IPDoNotFragment=maybe
            IPDoNotFragment=2
            IPDoNotFragment=INHERIT
            IPDoNotFragment=inherit_
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }
}
