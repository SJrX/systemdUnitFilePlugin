package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseAdActorSystemOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidMacAddresses() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Bond]
            AdActorSystem=00:11:22:33:44:55
            AdActorSystem=AA:BB:CC:DD:EE:FF
            AdActorSystem=01:23:45:67:89:ab
            AdActorSystem=FE:DC:BA:98:76:54
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidMacAddresses() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Bond]
            AdActorSystem=<error descr="Invalid value">00:11:22:33:44</error>
            AdActorSystem=<error descr="Invalid value">00:11:22:33:44:55:66</error>
            AdActorSystem=<error descr="Invalid value">00-11-22-33-44-55</error>
            AdActorSystem=<error descr="Invalid value">GG:HH:II:JJ:KK:LL</error>
            AdActorSystem=<error descr="Invalid value">0:1:2:3:4:5</error>
            AdActorSystem=<error descr="Invalid value">001122334455</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(6, highlights)
    }

    @Test
    fun testMixedCaseIsValid() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Bond]
            AdActorSystem=aA:bB:cC:dD:eE:fF
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }
}
