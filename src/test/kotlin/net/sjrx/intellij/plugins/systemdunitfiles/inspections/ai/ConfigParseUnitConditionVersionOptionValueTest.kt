package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseUnitConditionVersionOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // An operator-less token is treated as an fnmatch glob, so any non-empty string is legitimate.
        // Covers both ConditionKernelVersion= and ConditionVersion= (shared ltype CONDITION_VERSION).
        // language="unit file (systemd)"
        val file = """
            [Unit]
            ConditionKernelVersion=>=5.4
            ConditionKernelVersion=6.*
            ConditionVersion=systemd >= 250
            ConditionVersion=glibc >= 2.31
            AssertKernelVersion=<6.0
            AssertVersion=5.15
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
            ConditionKernelVersion=!5.4
            ConditionKernelVersion=|6.1
            ConditionKernelVersion=|!5.10
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }
}
