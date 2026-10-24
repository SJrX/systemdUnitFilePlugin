package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** [RoutingPolicyRule] FirewallMark=. */
class ConfigParseRoutingPolicyRuleFirewallMarkOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [RoutingPolicyRule]
            FirewallMark=1011
            FirewallMark=7/255
            FirewallMark=0x10/0xff
            FirewallMark=0
            FirewallMark=4294967295/4294967295
            FirewallMark=0b101
            FirewallMark=7/ 255
            FirewallMark=0b 1
            FirewallMark=1/0b 1
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidValues() {
    // One highlight per line. sd-check rejects +0b1, 1/+0b1 and 1 /2, and accepts 0b 1 and 1/0b 1.
    // language="unit file (systemd)"
    val file = """
            [RoutingPolicyRule]
            FirewallMark=4294967296
            FirewallMark=7/
            FirewallMark=/255
            FirewallMark=mark
            FirewallMark=-1
            FirewallMark=+0b1
            FirewallMark=1/+0b1
            FirewallMark=1 /2
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(8, myFixture.doHighlighting())
  }
}
