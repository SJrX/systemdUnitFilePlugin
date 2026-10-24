package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseBadadvBandwidthOptionValueTest : AbstractUnitFileTest() {

    @Test
    fun testValidValues() {
        // language="unit file (systemd)"
        val file = """
            [BatmanAdvanced]
            GatewayBandwidthDown=205M
            GatewayBandwidthUp=2G
            GatewayBandwidthDown=1000
            GatewayBandwidthUp=1.5G
            GatewayBandwidthDown=10.M
            GatewayBandwidthUp=3. K
            GatewayBandwidthDown=4 M 11.5K
            GatewayBandwidthUp=3.5G3B
            GatewayBandwidthDown=4T3G3B
            GatewayBandwidthUp=+5K
            GatewayBandwidthDown=429496729599999
            GatewayBandwidthUp=429T 496G 729M 599K 999
            GatewayBandwidthDown=429.496729599999T
            GatewayBandwithDown=100K
            GatewayBandwithUp=1T
            GatewayBandwidthDown=111.4
        """.trimIndent()

        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(0, highlights)
    }

    @Test
    fun testInvalidValues() {
        // language="unit file (systemd)"
        val file = """
            [BatmanAdvanced]
            GatewayBandwidthDown=5k
            GatewayBandwidthUp=-1
            GatewayBandwidthDown=12X
            GatewayBandwidthUp=3B3.5G
            GatewayBandwidthDown=12P12P
            GatewayBandwidthUp=3. 0 K
            GatewayBandwidthDown=fast
            GatewayBandwithUp=1 Mbit
            GatewayBandwidthDown=12P
            GatewayBandwidthUp=3E 2P
            GatewayBandwidthDown=429496729600000
            GatewayBandwidthUp=429.4967296T
            GatewayBandwidthDown=0.5P
            GatewayBandwidthUp=20E
            GatewayBandwidthDown=18446744073709551616
        """.trimIndent()

        setupFileInEditor("file.netdev", file)
        enableInspection(InvalidValueInspection::class.java)
        val highlights = myFixture.doHighlighting()

        assertSize(15, highlights)
    }
}
