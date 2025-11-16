package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseGeneveDfOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidEnumValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [GENEVE]
            IPDoNotFragment=no
            IPDoNotFragment=yes
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
    fun testValidBooleanValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [GENEVE]
            IPDoNotFragment=1
            IPDoNotFragment=0
            IPDoNotFragment=true
            IPDoNotFragment=false
            IPDoNotFragment=on
            IPDoNotFragment=off
            IPDoNotFragment=y
            IPDoNotFragment=n
            IPDoNotFragment=t
            IPDoNotFragment=f
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
            [GENEVE]
            IPDoNotFragment=maybe
            IPDoNotFragment=auto
            IPDoNotFragment=2
            IPDoNotFragment=never
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }
}
