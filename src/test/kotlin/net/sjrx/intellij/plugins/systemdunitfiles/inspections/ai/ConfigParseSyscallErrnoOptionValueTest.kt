package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseSyscallErrnoOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            SystemCallErrorNumber=kill
            SystemCallErrorNumber=EPERM
            SystemCallErrorNumber=ENOENT
            SystemCallErrorNumber=EACCES
            SystemCallErrorNumber=EAGAIN
            SystemCallErrorNumber=1
            SystemCallErrorNumber=13
            SystemCallErrorNumber=0
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
            SystemCallErrorNumber=<error descr="Invalid value">KILL</error>
            SystemCallErrorNumber=<error descr="Invalid value">eperm</error>
            SystemCallErrorNumber=<error descr="Invalid value">PERM</error>
            SystemCallErrorNumber=<error descr="Invalid value">-1</error>
            SystemCallErrorNumber=<error descr="Invalid value">99999</error>
            SystemCallErrorNumber=<error descr="Invalid value">EPERM ENOENT</error>
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(6, highlights)
    }
}
