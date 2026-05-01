package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseWolOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidOff() {
        // language="unit file (systemd)"
        val file = """
            [Link]
            WakeOnLan=off
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()
        assertSize(0, highlights)
    }

    @Test
    fun testValidSingleFlags() {
        // language="unit file (systemd)"
        val file = """
            [Link]
            WakeOnLan=phy
            WakeOnLan=unicast
            WakeOnLan=multicast
            WakeOnLan=broadcast
            WakeOnLan=arp
            WakeOnLan=magic
            WakeOnLan=secureon
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()
        assertSize(0, highlights)
    }

    @Test
    fun testValidMultipleFlags() {
        // language="unit file (systemd)"
        val file = """
            [Link]
            WakeOnLan=phy magic
            WakeOnLan=unicast multicast broadcast
            WakeOnLan=phy unicast multicast broadcast arp magic secureon
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidUnknownFlag() {
        // language="unit file (systemd)"
        val file = """
            [Link]
            WakeOnLan=bogus
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()
        assertSize(1, highlights)
    }

    @Test
    fun testInvalidMixedWithUnknown() {
        // language="unit file (systemd)"
        val file = """
            [Link]
            WakeOnLan=phy bogus
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()
        assertSize(1, highlights)
    }

    @Test
    fun testInvalidCommaSeparator() {
        // language="unit file (systemd)"
        val file = """
            [Link]
            WakeOnLan=phy,magic
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()
        assertSize(1, highlights)
    }

    @Test
    fun testInvalidOffMixed() {
        // off is only valid as a standalone value
        // language="unit file (systemd)"
        val file = """
            [Link]
            WakeOnLan=off phy
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()
        assertSize(1, highlights)
    }
}
