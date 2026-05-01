package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseTriggerUnitOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidUnitNames() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Path]
            Unit=foo.service
            Unit=bar.socket
            Unit=baz.target
            Unit=my-app.mount
            Unit=net_dev.automount
            Unit=tmp.timer
            Unit=swapfile.swap
            Unit=watcher.path
            Unit=workload.slice
            Unit=session.scope
            Unit=fs-home.device
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.path", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testValidTemplateAndComplexNames() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Path]
            Unit=getty@tty1.service
            Unit=user@1000.service
            Unit=foo.bar.service
            Unit=a-b_c.service
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.path", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(0, highlights)
    }

    @Test
    fun testInvalidUnitTypes() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Path]
            Unit=foo.bogus
            Unit=bar.unknown
            Unit=foo
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.path", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(3, highlights)
    }

    @Test
    fun testInvalidCharacters() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [Path]
            Unit=foo bar.service
            Unit=foo/bar.service
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.path", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(2, highlights)
    }
}
