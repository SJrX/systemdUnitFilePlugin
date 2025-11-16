package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseWireguardListenPortOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [WireGuard]
            ListenPort=auto
            ListenPort=1
            ListenPort=80
            ListenPort=8080
            ListenPort=65535
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [WireGuard]
            ListenPort=<error descr="Option has an invalid value">0</error>
            ListenPort=<error descr="Option has an invalid value">65536</error>
            ListenPort=<error descr="Option has an invalid value">99999</error>
            ListenPort=<error descr="Option has an invalid value">-1</error>
            ListenPort=<error descr="Option has an invalid value">Auto</error>
            ListenPort=<error descr="Option has an invalid value">AUTO</error>
            ListenPort=<error descr="Option has an invalid value">invalid</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(7, highlights)
    }
}
