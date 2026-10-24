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
            IPProtocol=aggfrag
            IPProtocol=AGGFRAG
            IPProtocol=144
            IPProtocol=0
            IPProtocol=58
            IPProtocol=TcP
            IPProtocol=IcMpV6
            IPProtocol=L2TP
            IPProtocol=0b 110
            IPProtocol=-0
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidValues() {
    // One highlight per line. sd-check rejects +0b110 (EINVAL) and 0b-1 (-1: ERANGE), and accepts 0b 110 (tcp) and -0.
    // language="unit file (systemd)"
    val file = """
            [RoutingPolicyRule]
            IPProtocol=tcpp
            IPProtocol=256
            IPProtocol=mptcp
            IPProtocol=-6
            IPProtocol=3
            IPProtocol=200
            IPProtocol=TcPx
            IPProtocol=Yes
            IPProtocol=+0b110
            IPProtocol=0b-1
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(10, myFixture.doHighlighting())
  }
}
