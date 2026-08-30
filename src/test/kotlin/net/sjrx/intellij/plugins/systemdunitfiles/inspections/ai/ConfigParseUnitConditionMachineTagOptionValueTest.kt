package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseUnitConditionMachineTagOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // A machine tag is matched with fnmatch against /etc/machine-info TAGS: any non-empty string is
        // a legitimate glob, so none of these are flagged.
        // language="unit file (systemd)"
        val file = """
            [Unit]
            ConditionMachineTag=production
            ConditionMachineTag=edge-*
            AssertMachineTag=staging
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
            ConditionMachineTag=!production
            ConditionMachineTag=|production
            ConditionMachineTag=|!production
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }
}
