package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseInterfacePairOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // From TEST-13-NSPAWN; an empty container-side name after the colon means `not given`.
        // language="unit file (systemd)"
        val file = """
            [Network]
            Interface=sd-shared1 sd-shared2:sd-renamed2 sd-shared3:sd-altname3
            MACVLAN=sd-macvlan1 sd-macvlan2:my-macvlan2 sd-macvlanloong
            IPVLAN=sd-ipvlan1 sd-ipvlan2:my-ipvlan2 sd-ipvlanlooong
            Interface=eth0:
            Interface=eth0    wlan0
            MACVLAN=enp0s31f6
            IPVLAN=a.b:c-d
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // Reserved name, all digits, a second colon, too long (either side), `/`, and a reserved name later in the list.
        // language="unit file (systemd)"
        val file = """
            [Network]
            Interface=all
            MACVLAN=0123
            IPVLAN=eth0:host0:x
            Interface=eth0:averyveryverylongname
            Interface=averyveryverylongname
            MACVLAN=eth/0
            IPVLAN=eth0 default
        """.trimIndent()

        setupFileInEditor("file.nspawn", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(7, highlights)
    }
}
