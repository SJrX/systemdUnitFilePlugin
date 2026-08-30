package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseUnitConditionKernelCommandLineOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Either a bare kernel-command-line option or a full name=value; the parameter is stored verbatim,
        // so any non-empty string is legitimate.
        // language="unit file (systemd)"
        val file = """
            [Unit]
            ConditionKernelCommandLine=quiet
            ConditionKernelCommandLine=debug
            ConditionKernelCommandLine=systemd.unified_cgroup_hierarchy=1
            AssertKernelCommandLine=rd.rescue
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
            ConditionKernelCommandLine=!quiet
            ConditionKernelCommandLine=|debug
            ConditionKernelCommandLine=|!quiet
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }
}
