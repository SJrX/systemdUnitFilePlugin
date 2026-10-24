package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import org.junit.Test

/**
 * systemd's parse_boolean() (src/basic/parse-util.c) compares with STRCASE_IN_SET, so `Yes`, `TRUE`
 * and `oFf` are as valid as `yes`. Settings that take "a boolean or one of these names" go through
 * string_table_lookup_from_string_with_boolean (src/basic/string-table.c), which tries parse_boolean()
 * first and then a *case-sensitive* streq() over the table: only the boolean half ignores case.
 *
 * Expectations are derived from the C at 0f0682b773. Each validator is exercised under whichever
 * grammar engine the test run selects (`-Dsystemd.unit.grammarParseEngine=true` forces the new one).
 */
class CaseInsensitiveBooleanInspectionTest : AbstractUnitFileTest() {

  private fun highlights(fileName: String, text: String): Int {
    setupFileInEditor(fileName, text)
    enableInspection(InvalidValueInspection::class.java)
    return myFixture.doHighlighting().size
  }

  private fun assertAccepted(fileName: String, vararg lines: String) =
    assertEquals(lines.joinToString(), 0, highlights(fileName, lines.joinToString("\n") + "\n"))

  /** Each of [values] for [key] in [section] is rejected on its own. */
  private fun assertEachRejected(fileName: String, section: String, key: String, vararg values: String) {
    for (value in values) {
      val text = "$section\n$key=$value\n"
      assertEquals(text, 1, highlights(fileName, text))
    }
  }

  @Test
  fun testPlainBooleanAcceptsAnyCasing() {
    assertAccepted(
      "f.service",
      "[Service]",
      "PrivatePIDs=Yes",
      "PrivatePIDs=TRUE",
      "PrivatePIDs=oFf",
      "PrivatePIDs=N",
      "PrivatePIDs=T",
      "PrivatePIDs=On",
    )
    assertEachRejected("f.service", "[Service]", "PrivatePIDs", "yess", "Truee", "2")
  }

  @Test
  fun testPrivateTmpBooleanHalfIgnoresCaseButNamesDoNot() {
    assertAccepted(
      "f.service",
      "[Service]",
      "PrivateTmp=True",
      "PrivateTmp=FALSE",
      "PrivateTmp=Yes",
      "PrivateTmp=oFf",
      "PrivateTmp=tRUE",
      "PrivateTmp=disconnected",
      "PrivateTmp=connected",
    )
    assertEachRejected("f.service", "[Service]", "PrivateTmp", "Disconnected", "DISCONNECTED", "Connected", "yess")
  }

  @Test
  fun testPrivateUsersAcceptsMixedCaseBoolean() {
    assertAccepted("f.service", "[Service]", "PrivateUsers=False", "PrivateUsers=TRUE", "PrivateUsers=oFf")
    assertAccepted("f.nspawn", "[Exec]", "PrivateUsers=False", "PrivateUsers=Yes", "PrivateUsers=pick")
    assertEachRejected("f.nspawn", "[Exec]", "PrivateUsers", "Pick", "Identity", "yess")
  }

  @Test
  fun testProtectSettings() {
    assertAccepted(
      "f.service",
      "[Service]",
      "ProtectSystem=Yes",
      "ProtectSystem=strict",
      "ProtectHome=TRUE",
      "ProtectHome=read-only",
      "ProtectControlGroups=On",
      "ProtectControlGroups=private",
      "ProtectHostname=Yes",
      "ProtectHostname=fALSE",
      "ProtectHostname=private",
      "ProtectHostname=tRUE:myhost",
      "ProtectHostname=Yes:my-host.example",
      "RuntimeDirectoryPreserve=Yes",
      "RuntimeDirectoryPreserve=on-success",
      "MemoryPressureWatch=On",
      "MemoryPressureWatch=skip",
      "RestrictNamespaces=tRUE",
      "RestrictNamespaces=~net ipc",
      "Delegate=Yes",
      "Delegate=cpu io",
    )
    assertEachRejected("f.service", "[Service]", "ProtectSystem", "Strict", "FULL")
    assertEachRejected("f.service", "[Service]", "ProtectHome", "Read-Only", "Tmpfs")
    assertEachRejected("f.service", "[Service]", "ProtectControlGroups", "Private")
    // A false boolean with a hostname is refused (t == PROTECT_HOSTNAME_NO && h), in any casing.
    assertEachRejected("f.service", "[Service]", "ProtectHostname", "Private", "No:myhost", "fALSE:myhost")
    assertEachRejected("f.service", "[Service]", "RuntimeDirectoryPreserve", "Restart")
    assertEachRejected("f.service", "[Service]", "MemoryPressureWatch", "Auto")
    assertEachRejected("f.service", "[Service]", "RestrictNamespaces", "NET")
  }

  @Test
  fun testSocketDeferTrigger() {
    assertAccepted("f.socket", "[Socket]", "DeferTrigger=TRUE", "DeferTrigger=patient")
    assertEachRejected("f.socket", "[Socket]", "DeferTrigger", "Patient")
  }

  @Test
  fun testConditionVirtualization() {
    assertAccepted(
      "f.service",
      "[Unit]",
      "ConditionVirtualization=No",
      "ConditionVirtualization=!Yes",
      "ConditionVirtualization=|tRUE",
      "ConditionVirtualization=none",
    )
  }

  @Test
  fun testNetworkSettings() {
    assertAccepted(
      "f.network",
      "[Network]",
      "DHCP=True",
      "DHCP=ipv4",
      "LinkLocalAddressing=Yes",
      "LinkLocalAddressing=fallback",
      "IPv6PrivacyExtensions=On",
      "IPv6PrivacyExtensions=prefer-public",
      "LLDP=TRUE",
      "LLDP=routers-only",
      "KeepConfiguration=Yes",
      "KeepConfiguration=dhcp-on-stop",
      "UseDomains=Yes",
      "UseDomains=route",
      "DNSSEC=Yes",
      "DNSSEC=allow-downgrade",
      "IPMasquerade=both",
      "IPMasquerade=No",
    )
    assertEachRejected("f.network", "[Network]", "DHCP", "IPv4", "yess")
    assertEachRejected("f.network", "[Network]", "LLDP", "Routers-Only")
    assertEachRejected("f.network", "[Network]", "KeepConfiguration", "Static")
    assertEachRejected("f.network", "[Network]", "DNSSEC", "Allow-Downgrade")
    assertEachRejected("f.network", "[Network]", "IPMasquerade", "IPv4", "Both")

    assertAccepted("f.network", "[Address]", "DuplicateAddressDetection=False", "DuplicateAddressDetection=none")
    assertEachRejected("f.network", "[Address]", "DuplicateAddressDetection", "None", "IPv6")

    assertAccepted("f.network", "[DHCPServer]", "PersistLeases=Yes", "PersistLeases=runtime")
    assertEachRejected("f.network", "[DHCPServer]", "PersistLeases", "Runtime")

    assertAccepted("f.network", "[IPv6AcceptRA]", "DHCPv6Client=Yes", "DHCPv6Client=always")
    assertEachRejected("f.network", "[IPv6AcceptRA]", "DHCPv6Client", "Always")
  }

  @Test
  fun testNetdevSettings() {
    assertAccepted("f.netdev", "[VXLAN]", "IPDoNotFragment=Yes", "IPDoNotFragment=inherit")
    assertEachRejected("f.netdev", "[VXLAN]", "IPDoNotFragment", "Inherit")

    assertAccepted("f.netdev", "[GENEVE]", "IPDoNotFragment=No", "IPDoNotFragment=inherit")
    assertEachRejected("f.netdev", "[GENEVE]", "IPDoNotFragment", "INHERIT")

    // [Bridge] MulticastRouter= lives in .network files. multicast_router's WITH_BOOLEAN "yes" value is
    // _MULTICAST_ROUTER_INVALID, so only false booleans are accepted.
    assertAccepted("f.network", "[Bridge]", "MulticastRouter=No", "MulticastRouter=FALSE", "MulticastRouter=query")
    assertEachRejected("f.network", "[Bridge]", "MulticastRouter", "Yes", "true", "Query")
  }

  @Test
  fun testNspawnSettings() {
    assertAccepted("f.nspawn", "[Exec]", "Timezone=Off", "Timezone=TRUE", "Timezone=symlink")
    assertEachRejected("f.nspawn", "[Exec]", "Timezone", "Copy", "Auto")

    assertAccepted("f.nspawn", "[Files]", "PrivateUsersOwnership=Yes", "PrivateUsersOwnership=chown")
    assertEachRejected("f.nspawn", "[Files]", "PrivateUsersOwnership", "Chown", "Map")
  }
}
