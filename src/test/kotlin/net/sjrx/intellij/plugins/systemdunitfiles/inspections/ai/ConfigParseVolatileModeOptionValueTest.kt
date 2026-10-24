package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseVolatileModeOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Table names plus every parse_boolean spelling (case-insensitive).
        // language="unit file (systemd)"
        val file = """
            [Files]
            Volatile=no
            Volatile=yes
            Volatile=state
            Volatile=overlay
            Volatile=true
            Volatile=false
            Volatile=1
            Volatile=0
            Volatile=on
            Volatile=off
            Volatile=Yes
            Volatile=TRUE
            Volatile=oFf
            Volatile=NO
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Table names are matched case-sensitively.
        // language="unit file (systemd)"
        val file = """
            [Files]
            Volatile=State
            Volatile=overlayfs
            Volatile=maybe
            Volatile=OVERLAY
            Volatile=stateful
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(5, highlights)
    }
}
