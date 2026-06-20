package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseColonSeparatedPathsOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            ExecSearchPath=/usr/local/bin
            ExecSearchPath=/usr/local/bin:/usr/bin
            ExecSearchPath=/opt/app/bin:/usr/local/bin:/usr/bin:/bin
            ExecSearchPath=/var/lib/%n
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
            [Service]
            ExecSearchPath=<error descr="Invalid value">relative/bin</error>
            ExecSearchPath=<error descr="Invalid value">/usr/bin relative/bin</error>
            ExecSearchPath=<error descr="Invalid value">/usr/bin:relative/bin</error>
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(3, highlights)
    }
}
