package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseCakeTristateOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [CAKE]
            UseRawPacketSize=yes
            UseRawPacketSize=no
            UseRawPacketSize=true
            UseRawPacketSize=false
            UseRawPacketSize=on
            UseRawPacketSize=off
            UseRawPacketSize=1
            UseRawPacketSize=0
            UseRawPacketSize=y
            UseRawPacketSize=n
            UseRawPacketSize=t
            UseRawPacketSize=f
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
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
            [CAKE]
            UseRawPacketSize=auto
            UseRawPacketSize=maybe
            UseRawPacketSize=invalid
            UseRawPacketSize=2
            UseRawPacketSize=enabled
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }
}
