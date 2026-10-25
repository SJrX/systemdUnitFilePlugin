package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import com.intellij.lang.annotation.HighlightSeverity
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.settings.ExperimentalSettings

/**
 * Values systemd accepts but warns about (#565). Each warned case was confirmed with steve-local/systemd-tools sd-check
 * at 0f0682b773; the C reference is on the validator.
 */
class ValueWarningInspectionTest : AbstractUnitFileTest() {

  /** (highlighted text, message) of every ValueWarning highlight; also asserts they are all WARNING severity. */
  private fun warnings(fileName: String, contents: String): List<Pair<String, String>> {
    setupFileInEditor(fileName, contents)
    enableInspection(ValueWarningInspection::class.java)
    val highlights = myFixture.doHighlighting().filter { it.inspectionToolId == "ValueWarning" }
    for (h in highlights) {
      assertEquals(h.description, HighlightSeverity.WARNING, h.severity)
    }
    return highlights.map { it.text to it.description }
  }

  private fun assertSilent(fileName: String, section: String, vararg lines: String) {
    for (line in lines) {
      assertEquals(line, emptyList<Pair<String, String>>(), warnings(fileName, "[$section]\n$line"))
    }
  }

  private fun single(fileName: String, contents: String): Pair<String, String> = warnings(fileName, contents).single()

  fun testObsoleteStandardOutput() {
    assertEquals(
      "syslog" to "StandardOutput=syslog is obsolete; systemd treats it as journal. Use journal instead.",
      single("a.service", "[Service]\nStandardOutput=syslog")
    )
    assertEquals(
      "syslog+console" to "StandardError=syslog+console is obsolete; systemd treats it as journal+console. Use journal+console instead.",
      single("a.service", "[Service]\nStandardError=syslog+console")
    )
    assertSilent(
      "a.service", "Service",
      "StandardOutput=journal", "StandardOutput=journal+console", "StandardError=kmsg", "StandardOutput=file:/var/log/x",
      "StandardInput=null", "StandardOutput=Syslog",
    )
  }

  fun testDoubleBangPrefix() {
    val (text, message) = single("a.service", "[Service]\nExecStart=!!/bin/true")
    assertEquals("!!", text)
    assertEquals("The !! prefix is no longer supported; systemd ignores it and runs the command as if it had no ! prefix. Remove !!.", message)
    assertEquals("-!!", single("a.service", "[Service]\nExecStartPre=-!!/bin/true").first)
    // "+!!" is refused outright (the path becomes "!/bin/true"); that is InvalidValue's error, not a warning here.
    assertSilent("a.service", "Service", "ExecStart=!/bin/true", "ExecStart=+/bin/true", "ExecStart=-@/bin/true true", "ExecStart=+!!/bin/true")
  }

  fun testUnknownEscapeSequences() {
    val (text, message) = single("a.service", "[Service]\nExecStart=/bin/echo \\q")
    assertEquals("\\q", text)
    assertEquals(
      "\\q is not a valid escape sequence; systemd keeps the backslash and logs \"Ignoring unknown escape sequences\". " +
        "Write \\\\ for a literal backslash.",
      message
    )
    assertEquals("\\$", single("a.service", "[Service]\nExecStart=/bin/sh -c \"echo \\\$x\"").first)
    // \x00 is refused by cunescape (NUL), so "\x" is kept verbatim.
    assertEquals("\\x", single("a.service", "[Service]\nExecStart=/bin/echo \\x0").first)
    assertEquals("\\8", single("a.service", "[Service]\nExecStart=/bin/echo \\8").first)
    assertEquals(listOf("\\d", "\\w"), warnings("a.service", "[Service]\nExecStart=/bin/grep -E \"\\d+\\w\"").map { it.first })
    assertSilent(
      "a.service", "Service",
      "ExecStart=/bin/echo \\n \\t \\\\ \\\" \\' \\a \\b \\f \\r \\v \\s",
      "ExecStart=/bin/echo \\x41 \\101 \\u00e9 \\U0001F600",
      "ExecStart=/bin/echo a \\;",
      "ExecStart=/bin/sh -c 'echo \$\$HOME'",
    )
  }

  fun testLegacyVarRun() {
    assertEquals(
      "/var/run/foo.pid" to "PIDFile=/var/run/foo.pid is below the legacy directory /var/run/; systemd rewrites it to " +
        "/run/foo.pid and logs a notice. Use /run/foo.pid instead.",
      single("a.service", "[Service]\nPIDFile=/var/run/foo.pid")
    )
    assertTrue(single("a.service", "[Service]\nPIDFile=//var/./run/foo.pid").second.contains("rewrites it to /run/foo.pid"))
    assertSilent("a.service", "Service", "PIDFile=/run/foo.pid", "PIDFile=foo.pid", "PIDFile=/var/runner/x.pid", "PIDFile=/var/lib/run/x.pid")

    assertTrue(single("a.socket", "[Socket]\nListenStream=/var/run/foo.sock").second.startsWith("ListenStream=/var/run/foo.sock is below"))
    assertTrue(single("a.socket", "[Socket]\nListenDatagram=/var/run/foo.sock").second.contains("/run/foo.sock"))
    assertTrue(single("a.socket", "[Socket]\nListenFIFO=/var/run/foo.fifo").second.contains("/run/foo.fifo"))
    // Only ListenFIFO= and the AF_UNIX socket paths go through patch_var_run().
    assertSilent(
      "a.socket", "Socket",
      "ListenStream=/run/foo.sock", "ListenStream=80", "ListenStream=@var/run/x", "ListenMessageQueue=/var/run/foo",
      "ListenSpecial=/var/run/foo",
    )
  }

  fun testKillModeNone() {
    assertEquals(
      "none" to "KillMode=none is unsafe and deprecated: systemd stops managing the service's processes, so they keep " +
        "running after the service is stopped or restarted. Use mixed or control-group instead.",
      single("a.service", "[Service]\nKillMode=none")
    )
    assertSilent("a.service", "Service", "KillMode=mixed", "KillMode=control-group", "KillMode=process")
  }

  fun testUserNobody() {
    val (text, message) = single("a.service", "[Service]\nUser=nobody")
    assertEquals("nobody", text)
    assertTrue(message, message.startsWith("User=nobody is not safe:"))
    assertTrue(message, message.endsWith("Use DynamicUser=yes or a dedicated system user instead."))
    assertTrue(single("a.socket", "[Socket]\nSocketUser=nobody").second.startsWith("SocketUser=nobody is not safe:"))
    // systemd only checks keys containing "User" and only the literal name.
    assertSilent("a.service", "Service", "Group=nogroup", "Group=nobody", "User=65534", "User=nobody2", "User=daemon")
    assertSilent("a.socket", "Socket", "SocketGroup=nobody")
  }

  fun testAddressWithoutPrefixLength() {
    assertEquals(
      "10.0.0.1" to "Address=10.0.0.1 has no prefix length; systemd assumes /32 and logs a warning. Write the prefix " +
        "length explicitly, e.g. 10.0.0.1/32 for a single address or the length of the subnet.",
      single("a.network", "[Network]\nAddress=10.0.0.1")
    )
    assertTrue(single("a.network", "[Address]\nAddress=2001:db8::1").second.contains("assumes /128"))
    assertTrue(single("a.network", "[Address]\nPeer=10.0.0.2").second.startsWith("Peer=10.0.0.2 has no prefix length"))
    assertSilent("a.network", "Network", "Address=10.0.0.1/24", "Address=2001:db8::1/64", "Address=0.0.0.0/0")
    assertSilent("a.network", "Address", "Address=10.0.0.1/24", "Peer=10.0.0.2/32")
  }

  fun testWireGuardAllowedIpsNotMasked() {
    val found = warnings(
      "a.netdev",
      "[WireGuardPeer]\nAllowedIPs=192.168.26.3/24,10.0.0.0/8 fd31::1/48"
    )
    assertEquals(
      listOf(
        "192.168.26.3/24" to "AllowedIPs entry 192.168.26.3/24 has host bits set beyond the prefix length; systemd uses " +
          "192.168.26.0/24 instead. Write 192.168.26.0/24.",
        "fd31::1/48" to "AllowedIPs entry fd31::1/48 has host bits set beyond the prefix length; systemd uses fd31::/48 " +
          "instead. Write fd31::/48.",
      ),
      found
    )
    assertTrue(single("a.netdev", "[WireGuardPeer]\nAllowedIPs=10.1.2.0/0x10").second.contains("systemd uses 10.1.0.0/16"))
    assertSilent(
      "a.netdev", "WireGuardPeer",
      "AllowedIPs=10.0.0.0/8", "AllowedIPs=192.168.1.1", "AllowedIPs=0.0.0.0/0, ::/0", "AllowedIPs=fd31:bf08:57cb::/48",
      "AllowedIPs=10.1.0.0/0x10", "AllowedIPs=192.168.1.1/32",
    )
  }

  fun testIpMasqueradeTrueBoolean() {
    assertEquals(
      "yes" to "IPMasquerade= with a true boolean is deprecated; systemd treats it as ipv4, not both. Use ipv4 or both.",
      single("a.network", "[Network]\nIPMasquerade=yes")
    )
    // A false boolean silently means "no".
    assertSilent("a.network", "Network", "IPMasquerade=no", "IPMasquerade=off", "IPMasquerade=ipv4", "IPMasquerade=both")
  }

  fun testGrammarDeprecatedTokensAreReportedOnBothEngines() {
    // Formerly a weak-warning annotator behind the new-engine flag; now a WARNING regardless of the flag.
    for (flag in listOf(false, true)) {
      ExperimentalSettings.getInstance(project).state.useGrammarParseEngine = flag
      assertEquals("AF_DECnet", single("a.service", "[Service]\nRestrictAddressFamilies=AF_INET AF_DECnet").first)
      assertEquals("v4", single("a.network", "[Network]\nDHCP=v4").first)
      assertSilent("a.service", "Service", "RestrictAddressFamilies=AF_INET AF_INET6")
    }
  }

  fun testInvalidValueIsNotAlsoWarned() {
    // The value is invalid (InvalidValue reports it), so no deprecation is piled on top.
    assertSilent("a.service", "Service", "RestrictAddressFamilies=AF_DECnet AF_BOGUS", "PIDFile=/var/run/../x.pid")
    assertSilent("a.network", "Network", "Address=10.0.0.300")
  }
}
