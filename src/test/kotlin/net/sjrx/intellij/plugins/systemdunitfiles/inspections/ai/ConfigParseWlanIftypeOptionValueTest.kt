package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseWlanIftypeOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // Fixture Setup
        // language="unit file (systemd)"
        val file = """
            [WLAN]
            Type=ad-hoc
            Type=station
            Type=ap
            Type=ap-vlan
            Type=wds
            Type=monitor
            Type=mesh-point
            Type=p2p-client
            Type=p2p-go
            Type=p2p-device
            Type=ocb
            Type=nan
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
            [WLAN]
            Type=<error descr="Value must match: (ad-hoc|station|ap|ap-vlan|wds|monitor|mesh-point|p2p-client|p2p-go|p2p-device|ocb|nan)">invalid</error>
            Type=<error descr="Value must match: (ad-hoc|station|ap|ap-vlan|wds|monitor|mesh-point|p2p-client|p2p-go|p2p-device|ocb|nan)">managed</error>
            Type=<error descr="Value must match: (ad-hoc|station|ap|ap-vlan|wds|monitor|mesh-point|p2p-client|p2p-go|p2p-device|ocb|nan)">adhoc</error>
            Type=<error descr="Value must match: (ad-hoc|station|ap|ap-vlan|wds|monitor|mesh-point|p2p-client|p2p-go|p2p-device|ocb|nan)">p2p</error>
        """.trimIndent()

        // Execute SUT
        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        // Verification
        assertSize(4, highlights)
    }
}
