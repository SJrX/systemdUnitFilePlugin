package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** [RoutingPolicyRule] IPProtocol=. */
class ConfigParseRoutingPolicyRuleIpProtocolOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [RoutingPolicyRule]
            IPProtocol=tcp
            IPProtocol=UDP
            IPProtocol=sctp
            IPProtocol=icmpv6
            IPProtocol=6
            IPProtocol=17
            IPProtocol=0x11
            IPProtocol=255
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
            [RoutingPolicyRule]
            IPProtocol=tcpp
            IPProtocol=256
            IPProtocol=mptcp
            IPProtocol=-6
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(4, myFixture.doHighlighting())
  }
}
