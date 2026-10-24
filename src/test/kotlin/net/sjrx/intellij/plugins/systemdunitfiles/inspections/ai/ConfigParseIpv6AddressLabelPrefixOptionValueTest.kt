package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** [IPv6AddressLabel] Prefix=. */
class ConfigParseIpv6AddressLabelPrefixOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [IPv6AddressLabel]
            Prefix=2001:db8::/32
            Prefix=::/0
            Prefix=::ffff:0:0/96
            Prefix=fe80::1
            Prefix=2001:db8::/ 32
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
            [IPv6AddressLabel]
            Prefix=::ffff:0:0/97
            Prefix=::ffff:1.2.3.4
            Prefix=10.0.0.0/8
            Prefix=2001:db8::/200
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(4, myFixture.doHighlighting())
  }
}
