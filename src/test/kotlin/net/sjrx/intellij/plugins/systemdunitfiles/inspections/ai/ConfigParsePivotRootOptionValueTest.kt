package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParsePivotRootOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Absolute, normalized paths; a trailing slash and dot-prefixed names are fine.
        // language="unit file (systemd)"
        val file = """
            [Exec]
            PivotRoot=/sysroot
            PivotRoot=/sysroot/ostree/deploy:/sysroot
            PivotRoot=/new/:/old/
            PivotRoot=/new:
            PivotRoot=/
            PivotRoot=/a/.hidden:/b/..c
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // path_is_normalized refuses `//`, `.` and `..` components in either path; both must be absolute.
        // language="unit file (systemd)"
        val file = """
            [Exec]
            PivotRoot=relative
            PivotRoot=/a//b
            PivotRoot=/a/./b
            PivotRoot=/a/../b
            PivotRoot=/new:relative
            PivotRoot=/new:/old//x
            PivotRoot=:/old
            PivotRoot=/a/.
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(8, highlights)
    }
}
