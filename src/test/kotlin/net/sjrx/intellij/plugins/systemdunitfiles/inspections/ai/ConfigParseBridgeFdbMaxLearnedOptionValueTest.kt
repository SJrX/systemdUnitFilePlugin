package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseBridgeFdbMaxLearnedOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Bridge]
            FDBMaxLearned=0
            FDBMaxLearned=1
            FDBMaxLearned=100
            FDBMaxLearned=1000
            FDBMaxLearned=65535
            FDBMaxLearned=4294967295
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
            [Bridge]
            FDBMaxLearned=-1
            FDBMaxLearned=4294967296
            FDBMaxLearned=abc
            FDBMaxLearned=1.5
            FDBMaxLearned=0x100
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
            [Bridge]
            FDBMaxLearned=0
            FDBMaxLearned=4294967295
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidBoundary() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Bridge]
            FDBMaxLearned=4294967296
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }
}
