package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** [Neighbor] Address= (NEIGHBOR_DESTINATION_ADDRESS). */
class ConfigParseNeighborDestinationAddressOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [Neighbor]
            Address=192.168.10.1
            Address=2004:da8:1:0::1
            Address=0.0.0.0
            Address=fe80::1
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
            [Neighbor]
            Address=192.168.10.1/24
            Address=not-an-address
            Address=256.1.1.1
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(3, myFixture.doHighlighting())
  }
}
