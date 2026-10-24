package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** [RoutingPolicyRule] User=. */
class ConfigParseRoutingPolicyRuleUidRangeOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [RoutingPolicyRule]
            User=100-200
            User=1000
            User=0
            User=root
            User=systemd-network
            User=1000-1000
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
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(5, myFixture.doHighlighting())
  }
}
