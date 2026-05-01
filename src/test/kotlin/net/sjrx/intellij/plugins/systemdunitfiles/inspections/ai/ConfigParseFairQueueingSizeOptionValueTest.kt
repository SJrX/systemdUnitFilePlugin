package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseFairQueueingSizeOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [FairQueueing]
            QuantumBytes=1024
            InitialQuantumBytes=4096
            QuantumBytes=1K
            InitialQuantumBytes=64K
            QuantumBytes=1M
            InitialQuantumBytes=2G
            QuantumBytes=1T
            InitialQuantumBytes=0
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
            [FairQueueing]
            QuantumBytes=abc
            InitialQuantumBytes=1024L
            QuantumBytes=10.5M
            InitialQuantumBytes=0x1000
            QuantumBytes=K
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(5, highlights)
    }
}
