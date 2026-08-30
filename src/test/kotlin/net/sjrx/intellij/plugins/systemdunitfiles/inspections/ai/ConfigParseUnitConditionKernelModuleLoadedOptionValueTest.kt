package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseUnitConditionKernelModuleLoadedOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Unit]
            ConditionKernelModuleLoaded=nf_tables
            ConditionKernelModuleLoaded=nf-tables
            ConditionKernelModuleLoaded=overlay
            ConditionKernelModuleLoaded=foo.bar
            AssertKernelModuleLoaded=btrfs
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Unit]
            ConditionKernelModuleLoaded=foo/bar
            ConditionKernelModuleLoaded=.
            ConditionKernelModuleLoaded=..
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(3, highlights)
    }
}
