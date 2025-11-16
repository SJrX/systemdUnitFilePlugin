package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseL2tpSessionIdOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [L2TPSession]
            PeerSessionId=1
            PeerSessionId=100
            PeerSessionId=4294967295
            PeerSessionId=2147483647
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
            [L2TPSession]
            PeerSessionId=<error descr="Invalid option value">0</error>
            PeerSessionId=<error descr="Invalid option value">4294967296</error>
            PeerSessionId=<error descr="Invalid option value">-1</error>
            PeerSessionId=<error descr="Invalid option value">abc</error>
            PeerSessionId=<error descr="Invalid option value">1.5</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }
}
