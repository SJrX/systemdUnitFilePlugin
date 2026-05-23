package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseExecInputOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            StandardInput=null
            StandardInput=tty
            StandardInput=tty-force
            StandardInput=tty-fail
            StandardInput=socket
            StandardInput=data
            StandardInput=fd:stdin
            StandardInput=fd:
            StandardInput=file:/var/log/input.log
            StandardInput=file:/dev/null
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
            StandardInput=<error descr="Invalid value">stdin</error>
            StandardInput=<error descr="Invalid value">file:relative/path</error>
            StandardInput=<error descr="Invalid value">file:</error>
            StandardInput=<error descr="Invalid value">FD:stdin</error>
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(4, highlights)
    }
}
