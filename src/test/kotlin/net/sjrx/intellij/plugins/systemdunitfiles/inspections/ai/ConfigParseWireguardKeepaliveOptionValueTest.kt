package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseWireguardKeepaliveOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [WireGuardPeer]
            PersistentKeepalive=off
            PersistentKeepalive=0
            PersistentKeepalive=1
            PersistentKeepalive=25
            PersistentKeepalive=1000
            PersistentKeepalive=65535
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
            [WireGuardPeer]
            PersistentKeepalive=65536
            PersistentKeepalive=-1
            PersistentKeepalive=disabled
            PersistentKeepalive=100000
            PersistentKeepalive=yes
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }
    
    @Test
    fun testBoundaryValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [WireGuardPeer]
            PersistentKeepalive=0
            PersistentKeepalive=65535
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }
}
