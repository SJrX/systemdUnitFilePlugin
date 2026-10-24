package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseCoalesceNsecOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [Link]
            RxCoalesceCQENSec=500
            RxCoalesceCQENSec=1us
            RxCoalesceCQENSec=2.5
            RxCoalesceCQENSec=1s500ms
            RxCoalesceCQENSec=.7
            RxCoalesceCQENSec=3.1 sec .2 sec
            RxCoalesceCQENSec=+3.1s
            RxCoalesceCQENSec=3.1s.2
            RxCoalesceCQENSec=100 ns
            RxCoalesceCQENSec=1µs
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [Link]
            RxCoalesceCQENSec=infinity
            RxCoalesceCQENSec=-5
            RxCoalesceCQENSec=5.
            RxCoalesceCQENSec=3.s
            RxCoalesceCQENSec=xyz
            RxCoalesceCQENSec=12..34
        """.trimIndent()

        setupFileInEditor("file.link", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(6, highlights)
    }
}
