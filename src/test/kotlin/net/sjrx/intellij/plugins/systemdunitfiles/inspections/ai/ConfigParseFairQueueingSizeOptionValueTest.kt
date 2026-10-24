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
            InitialQuantumBytes=0
            # parse_size() takes fractions and several elements (sd-check accepts these; 10.5M was asserted
            # invalid before #568).
            QuantumBytes=10.5M
            QuantumBytes=1G 512M
            QuantumBytes=4294967295
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
            InitialQuantumBytes=0x1000
            QuantumBytes=K
            # More than UINT32_MAX bytes is "too large" (sd-check); 1T was asserted valid before #568.
            QuantumBytes=1T
            InitialQuantumBytes=4G
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.network", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(6, highlights)
    }
}
