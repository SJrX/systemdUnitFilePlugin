package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParsePrivatePidsOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidBooleanValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            PrivatePIDs=yes
            PrivatePIDs=no
            PrivatePIDs=true
            PrivatePIDs=false
            PrivatePIDs=on
            PrivatePIDs=off
            PrivatePIDs=1
            PrivatePIDs=0
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
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
            [Swap]
            PrivatePIDs=invalid
            PrivatePIDs=maybe
            PrivatePIDs=2
            PrivatePIDs=enable
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }

    @Test
    fun testValidBooleanVariants() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            PrivatePIDs=y
            PrivatePIDs=n
            PrivatePIDs=t
            PrivatePIDs=f
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }
}
