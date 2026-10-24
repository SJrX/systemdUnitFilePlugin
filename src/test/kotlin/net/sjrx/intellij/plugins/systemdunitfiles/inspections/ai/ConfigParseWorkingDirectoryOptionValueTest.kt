package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseWorkingDirectoryOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            WorkingDirectory=~
            WorkingDirectory=/var/lib/myapp
            WorkingDirectory=/tmp
            WorkingDirectory=/
            WorkingDirectory=-/var/cache/myapp
            WorkingDirectory=-~
            WorkingDirectory=/var/lib/%n
            WorkingDirectory=/path with space
            WorkingDirectory=%S/osmocom
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
            WorkingDirectory=<error descr="Invalid value">relative/path</error>
            WorkingDirectory=<error descr="Invalid value">somepath</error>
            WorkingDirectory=<error descr="Invalid value">--/path</error>
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(3, highlights)
    }
}
