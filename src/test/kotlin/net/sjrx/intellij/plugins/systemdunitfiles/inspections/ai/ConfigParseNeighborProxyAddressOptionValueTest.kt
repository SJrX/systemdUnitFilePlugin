package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** config_parse_neighbor_proxy_address. */
class ConfigParseNeighborProxyAddressOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [Network]
            ProxyNeighbor=192.168.1.10
            ProxyNeighbor=2001:db8::1
            ProxyNeighbor=::ffff:10.0.0.1
            ProxyNeighbor=255.255.255.254
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidValues() {
    // One highlight per line.
    // language="unit file (systemd)"
    val file = """
            [Network]
            ProxyNeighbor=0.0.0.0
            ProxyNeighbor=::
            ProxyNeighbor=224.0.0.1
            ProxyNeighbor=ff02::1
            ProxyNeighbor=255.255.255.255
            ProxyNeighbor=192.168.1.0/24
            ProxyNeighbor=fe80::1%eth0
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(7, myFixture.doHighlighting())
  }
}
