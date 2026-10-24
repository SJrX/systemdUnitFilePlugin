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
            Prefix=::/0b 1
            Prefix=::ffff:0:0/0b1100000
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidValues() {
    // One highlight per line. sd-check rejects ::/+0b1 (EINVAL) and ::ffff:0:0/0b1100001 (97 > 96), and accepts ::/0b 1.
    // language="unit file (systemd)"
    val file = """
            [IPv6AddressLabel]
            Prefix=::ffff:0:0/97
            Prefix=::ffff:1.2.3.4
            Prefix=10.0.0.0/8
            Prefix=2001:db8::/200
            Prefix=::/+0b1
            Prefix=::ffff:0:0/0b1100001
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(6, myFixture.doHighlighting())
  }
}
