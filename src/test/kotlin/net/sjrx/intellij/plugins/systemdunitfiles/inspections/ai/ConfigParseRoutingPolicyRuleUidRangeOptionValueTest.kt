package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** [RoutingPolicyRule] User=. */
class ConfigParseRoutingPolicyRuleUidRangeOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // systemd-network and +1000 are accepted as possible user names: systemd resolves them via NSS at runtime
    // (get_user_creds), so they are only rejected on a machine without that user.
    // language="unit file (systemd)"
    val file = """
            [RoutingPolicyRule]
            User=100-200
            User=1000
            User=0
            User=root
            User=systemd-network
            User=1000-1000
            User=+1000
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
            User=200-100
            User=65535
            User=4294967295
            User=01000
            User=100-
            User=1000-99999999999999999999
            User=99999999999999999999-1000
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(7, myFixture.doHighlighting())
  }
}
