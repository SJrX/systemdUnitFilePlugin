package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseMemoryPressureWatchOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidBooleanValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            MemoryPressureWatch=yes
            MemoryPressureWatch=no
            MemoryPressureWatch=1
            MemoryPressureWatch=0
            MemoryPressureWatch=true
            MemoryPressureWatch=false
            MemoryPressureWatch=on
            MemoryPressureWatch=off
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidSpecialValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            MemoryPressureWatch=auto
            MemoryPressureWatch=skip
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidShortBooleanValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            MemoryPressureWatch=y
            MemoryPressureWatch=n
            MemoryPressureWatch=t
            MemoryPressureWatch=f
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
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
            [Swap]
            MemoryPressureWatch=invalid
            MemoryPressureWatch=maybe
            MemoryPressureWatch=2
            MemoryPressureWatch=automatic
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }
}
