package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDisableControllersOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidSingleController() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            DisableControllers=cpu
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidMultipleControllers() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            DisableControllers=cpu memory io
            DisableControllers=pids cpuset
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidBpfControllers() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            DisableControllers=bpf-firewall bpf-devices
            DisableControllers=bpf-foreign bpf-socket-bind bpf-restrict-network-interfaces
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidAllControllers() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            DisableControllers=cpu cpuset io memory pids bpf-firewall bpf-devices bpf-foreign bpf-socket-bind bpf-restrict-network-interfaces
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidController() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            DisableControllers=invalid
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }

    @Test
    fun testMixedValidAndInvalidControllers() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            DisableControllers=cpu invalid memory
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(1, highlights)
    }

    @Test
    fun testInvalidMultipleControllers() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            DisableControllers=foo bar
            DisableControllers=baz
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(2, highlights)
    }

    @Test
    fun testInvalidTypo() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Swap]
            DisableControllers=cpuu
            DisableControllers=bpf_firewall
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.swap", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(2, highlights)
    }
}
