package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseUnitConditionEnvironmentOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Either a bare variable name or a full NAME=value; the parameter is compared verbatim, so any
        // non-empty string is legitimate.
        // language="unit file (systemd)"
        val file = """
            [Unit]
            ConditionEnvironment=MY_VAR
            ConditionEnvironment=MY_VAR=1
            ConditionEnvironment=PATH=/usr/bin
            AssertEnvironment=DEBUG=true
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testValidWithMarkers() {
        // language="unit file (systemd)"
        val file = """
            [Unit]
            ConditionEnvironment=!MY_VAR
            ConditionEnvironment=|MY_VAR
            ConditionEnvironment=|!MY_VAR=1
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }
}
