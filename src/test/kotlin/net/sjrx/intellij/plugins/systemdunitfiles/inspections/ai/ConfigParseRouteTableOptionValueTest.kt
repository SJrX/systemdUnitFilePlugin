package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** [Route] Table= and [RoutingPolicyRule] Table=. */
class ConfigParseRouteTableOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // custom-table, my_table and 0x0 are deliberate leniencies: they are legal networkd.conf RouteTable= names
    // (only all-digit names are refused), which the plugin cannot see. systemd without such a definition rejects them.
    // language="unit file (systemd)"
    val file = """
            [Route]
            Table=main
            Table=local
            Table=default
            Table=1011
            Table=4294967295
            Table=custom-table
            Table=0x0
            [RoutingPolicyRule]
            Table=7
            Table=my_table
            Table=0x10
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
            [Route]
            Table=0
            Table=4294967296
            Table=a:b
            [RoutingPolicyRule]
            Table=00
            Table=main table
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(5, myFixture.doHighlighting())
  }
}
