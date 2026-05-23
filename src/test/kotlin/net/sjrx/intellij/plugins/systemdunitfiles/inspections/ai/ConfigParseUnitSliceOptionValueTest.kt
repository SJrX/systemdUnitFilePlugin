package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseUnitSliceOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            Slice=system.slice
            Slice=user-1000.slice
            Slice=user.slice
            Slice=machine.slice
            Slice=app-myapp.slice
            Slice=-.slice
            Slice=%p.slice
            Slice=user-%i.slice
            Slice=%n
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Service]
            Slice=<error descr="Invalid value">system.target</error>
            Slice=<error descr="Invalid value">system.service</error>
            Slice=<error descr="Invalid value">noextension</error>
            Slice=<error descr="Invalid value">system.slice extra</error>
        """.trimIndent()

        setupFileInEditor("file.service", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(4, highlights)
    }
}
