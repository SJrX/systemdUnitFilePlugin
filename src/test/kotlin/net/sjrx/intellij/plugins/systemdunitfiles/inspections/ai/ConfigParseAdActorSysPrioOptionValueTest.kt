package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseAdActorSysPrioOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Bond]
            AdActorSystemPriority=1
            AdActorSystemPriority=100
            AdActorSystemPriority=32768
            AdActorSystemPriority=65535
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
            [Bond]
            AdActorSystemPriority=<error descr="Invalid value">0</error>
            AdActorSystemPriority=<error descr="Invalid value">65536</error>
            AdActorSystemPriority=<error descr="Invalid value">100000</error>
            AdActorSystemPriority=<error descr="Invalid value">-1</error>
            AdActorSystemPriority=<error descr="Invalid value">abc</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }
}
