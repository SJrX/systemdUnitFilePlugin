package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** config_parse_in_addr_non_null(AF_INET6). */
class ConfigParseInAddrNonNullIpv6OptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [Network]
            IPv6StableSecretAddress=2001:db8::1
            IPv6StableSecretAddress=::1
            IPv6StableSecretAddress=fe80::1234:5678
            IPv6StableSecretAddress=::ffff:192.168.1.1
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
            IPv6StableSecretAddress=::
            IPv6StableSecretAddress=0:0:0:0:0:0:0:0
            IPv6StableSecretAddress=192.168.1.1
            IPv6StableSecretAddress=2001:db8::/64
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(4, myFixture.doHighlighting())
  }
}
