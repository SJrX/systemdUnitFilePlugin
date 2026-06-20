package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDelegateSubgroupOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language=unit file (systemd)
        val file = """
            [Service]
            DelegateSubgroup=supervisor
            DelegateSubgroup=my-subgroup
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // cg_needs_escape rejects names starting with '_'/'.' or containing '/', the "cgroup."
        // prefix, and the reserved names tasks/release_agent/notify_on_release.
        // language=unit file (systemd)
        val file = """
            [Service]
            DelegateSubgroup=_leading-underscore
            DelegateSubgroup=has/slash
            DelegateSubgroup=cgroup.controllers
            DelegateSubgroup=tasks
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(4, highlights)
    }
}
