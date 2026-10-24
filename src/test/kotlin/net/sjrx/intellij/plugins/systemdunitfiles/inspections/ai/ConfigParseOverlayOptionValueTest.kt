package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseOverlayOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // From TEST-13-NSPAWN and systemd-nspawn(1): an empty upper directory, relative sources, `+` prefixes, escaped colons.
        // language="unit file (systemd)"
        val file = """
            [Files]
            Overlay=+/var::/var
            Overlay=/tmp/1:/tmp/2:/tmp/3:/var
            OverlayReadOnly=/tmp/1:/tmp/2:/tmp/3:/var
            Overlay=/lower:/upper
            Overlay=lower:upper
            Overlay=/l1:/l2::/dest
            OverlayReadOnly=+/usr:/tmp/upper:/usr
            Overlay=/a\:b:/c
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Fewer than two paths, empty lower directories, and a destination that is not absolute
        // (with three or more paths the destination keeps any `+`, so `+/c` is not absolute).
        // language="unit file (systemd)"
        val file = """
            [Files]
            Overlay=/only
            Overlay=/a:
            Overlay=:/b
            Overlay=/a:/b:relative
            Overlay=/a::/b:/c
            OverlayReadOnly=/a:/b:+/c
            Overlay=+x:/b
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(7, highlights)
    }
}
