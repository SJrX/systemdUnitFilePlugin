package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseExecSelinuxContextOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language=unit file (systemd)
        val file = """
            [Service]
            SELinuxContext=system_u:system_r:httpd_t:s0
            SELinuxContext=-system_u:object_r:foo_t
            SELinuxContext=context for %i
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Unknown specifier (%e) and a trailing bare '%'.
        // language=unit file (systemd)
        val file = """
            [Service]
            SELinuxContext=bad %e specifier
            SELinuxContext=ends with %
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(2, highlights)
    }
}
