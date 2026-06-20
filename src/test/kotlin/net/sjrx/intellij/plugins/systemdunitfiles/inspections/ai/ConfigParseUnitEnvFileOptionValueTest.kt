package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseUnitEnvFileOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            EnvironmentFile=/etc/myapp.env
            EnvironmentFile=-/etc/myapp/optional.env
            EnvironmentFile=/etc/sysconfig/myapp
            EnvironmentFile=/var/lib/%n.env
            EnvironmentFile=-/run/secrets/myapp.env
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
            EnvironmentFile=<error descr="Invalid value">relative/path.env</error>
            EnvironmentFile=<error descr="Invalid value">myapp.env</error>
            EnvironmentFile=<error descr="Invalid value">~/myapp.env</error>
            EnvironmentFile=<error descr="Invalid value">--/etc/myapp.env</error>
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(4, highlights)
    }
}
