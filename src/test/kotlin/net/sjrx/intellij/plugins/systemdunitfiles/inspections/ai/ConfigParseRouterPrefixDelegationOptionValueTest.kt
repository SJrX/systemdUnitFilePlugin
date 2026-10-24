package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** config_parse_router_prefix_delegation (IPv6SendRA= and deprecated IPv6PrefixDelegation=). */
class ConfigParseRouterPrefixDelegationOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [Network]
            IPv6SendRA=yes
            IPv6SendRA=no
            IPv6SendRA=True
            IPv6SendRA=0
            IPv6SendRA=Yes
            IPv6SendRA=TRUE
            IPv6SendRA=oFf
            IPv6PrefixDelegation=static
            IPv6PrefixDelegation=dhcpv6
            IPv6PrefixDelegation=yes
            IPv6PrefixDelegation=Yes
            IPv6PrefixDelegation=TRUE
            IPv6PrefixDelegation=oFf
            IPv6PrefixDelegation=No
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
            IPv6SendRA=maybe
            IPv6SendRA=both
            IPv6SendRA=static
            IPv6SendRA=dhcpv6
            IPv6PrefixDelegation=Static
            IPv6PrefixDelegation=DHCPv6
            IPv6PrefixDelegation=maybe
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(7, myFixture.doHighlighting())
  }
}
