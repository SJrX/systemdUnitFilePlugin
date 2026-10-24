package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.CgroupSocketBindOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.EmergencyActionOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.NumaPolicyOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.PathOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SocketBindOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SocketProtocolOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseColonSeparatedPathsOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseDhcpSocketPriorityOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseDisableControllersOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseTriggerUnitOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseUnitMountsForOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseUnitSliceOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai.ConfigParseUnitStringPrintfOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarEngineVerdict
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarOptionValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Verdicts for the unit / service / cgroup validators fixed in #568, checked on the grammar under both engines.
 * Every value was confirmed with steve-local/systemd-tools/bin/sd-check at 0f0682b773, one value per file.
 */
class Fix568UnitGrammarVerdictsTest {

  private fun accepted(optionValue: GrammarOptionValue, vararg values: String) {
    for (value in values) {
      assertEquals("classic engine on <$value>", GrammarEngineVerdict.VALID, GrammarEngineVerdict.classic(optionValue.combinator, value))
      assertEquals("parse engine on <$value>", GrammarEngineVerdict.VALID, GrammarEngineVerdict.parseEngine(optionValue.combinator, value))
    }
  }

  private fun rejected(optionValue: GrammarOptionValue, vararg values: String) {
    for (value in values) {
      assertNotEquals("classic engine on <$value>", GrammarEngineVerdict.VALID, GrammarEngineVerdict.classic(optionValue.combinator, value))
      assertNotEquals("parse engine on <$value>", GrammarEngineVerdict.VALID, GrammarEngineVerdict.parseEngine(optionValue.combinator, value))
    }
  }

  @Test
  fun testEmergencyActionTakesTheWholeSystemTable() {
    accepted(
      EmergencyActionOptionValue(),
      "none", "exit", "exit-force", "reboot", "reboot-force", "reboot-immediate", "poweroff", "poweroff-force",
      "poweroff-immediate", "soft-reboot", "soft-reboot-force", "kexec", "kexec-force", "halt", "halt-force", "halt-immediate"
    )
    rejected(EmergencyActionOptionValue(), "Halt", "halt-now", "foo", "reboot force")
  }

  @Test
  fun testNumaPolicyTakesTheWholeMpolTable() {
    accepted(NumaPolicyOptionValue(), "default", "preferred", "bind", "interleave", "local", "preferred-many", "weighted-interleave")
    rejected(NumaPolicyOptionValue(), "Bind", "1", "prefer")
  }

  @Test
  fun testSocketProtocol() {
    accepted(SocketProtocolOptionValue(), "udplite", "sctp", "mptcp", "MPTCP", "UdpLite", "136", "132", "262", "0x84", "+136")
    // 0206 is octal 134; tcp and 6 are real protocols that SocketProtocol= does not allow.
    rejected(SocketProtocolOptionValue(), "0206", "tcp", "6", "foo")
  }

  @Test
  fun testBindIpv6OnlyTakesBooleans() {
    accepted(SocketBindOptionValue(), "default", "both", "ipv6-only", "false", "TRUE", "yes", "1", "off")
    rejected(SocketBindOptionValue(), "Both", "x", "ipv6")
  }

  @Test
  fun testDisableControllersNeedsOnlyOneKnownController() {
    accepted(
      ConfigParseDisableControllersOptionValue(),
      "blkio", "cpuacct", "devices", "cpu invalid memory", "foo cpu", "cpu foo", "foo bar io", "bpf-bind-network-interface"
    )
    rejected(ConfigParseDisableControllersOptionValue(), "invalid", "foo bar", "CPU", "'cpu'", "cpux", "foo cpux")
  }

  @Test
  fun testUnitPathPrintf() {
    accepted(PathOptionValue(), "/", "/hoge/with space", "/a/./b", "//a", "%h/x", "%i", "/a\\b", "/a/b/", "/a%", "/a/..b")
    rejected(PathOptionValue(), "/a/../b", "/a/..", "a", "/a/%Z", "%%")
  }

  @Test
  fun testUnitMountsFor() {
    accepted(ConfigParseUnitMountsForOptionValue(), "%f", "/tmp /var/log %h", "%t/x", "/a /b")
    rejected(ConfigParseUnitMountsForOptionValue(), "%Z", "a%h", "%%h", "/a/%Z", "/a/..", "/a/.. /b", "/a /b/..")
  }

  @Test
  fun testColonSeparatedPaths() {
    accepted(ConfigParseColonSeparatedPathsOptionValue(), "/tmp:/bin:/usr/bin:%V", "/a b:/c", "%h:/a", "/a")
    rejected(ConfigParseColonSeparatedPathsOptionValue(), "/a::/b", "/a:", ":/a", "/a/..:/b", "/a:%Z", "/a:b")
  }

  @Test
  fun testTriggerUnit() {
    accepted(
      ConfigParseTriggerUnitOptionValue(),
      "brltty@-etc-brltty_%i.conf.service", "a%i.service", "x@.service", "foo.service", "a.service.service", "a@b.timer"
    )
    rejected(ConfigParseTriggerUnitOptionValue(), "a%T.service", "@x.service", "a.foo", "a@b.mount", "a@.slice", "foo", "a b.service")
  }

  @Test
  fun testSliceCannotBeAnInstance() {
    accepted(ConfigParseUnitSliceOptionValue(), "a.slice", "-.slice", "a-b.slice", "a\\x2d.slice", "%p.slice")
    rejected(ConfigParseUnitSliceOptionValue(), "some@instance.slice", "a@.slice", "foo.service", "foo", "a b.slice", "%T.slice")
  }

  @Test
  fun testStringPrintfCopiesAPercentThatIsNotASpecifier() {
    accepted(ConfigParseUnitStringPrintfOptionValue(), "Ends with a percent %", "a %-x", "%%", "% x", "100%")
    rejected(ConfigParseUnitStringPrintfOptionValue(), "a %Z", "%1")
  }

  @Test
  fun testCgroupSocketBind() {
    accepted(
      CgroupSocketBindOptionValue(),
      "ipv4:tcp: 12345", "TCP", "Udp", "any", "ipv4:any", "tcp:any", "ipv4:tcp:any", "0", "0-0", "0--0", "1-65535",
      "ipv6:80-90", "1- 2", "0x50", "6", "ipv6:10000-65535", "ipv4"
    )
    rejected(
      CgroupSocketBindOptionValue(),
      "IPv4", "65536", "ipv4:", ":80", "ipv4:tcp:80:1", "tcp:ipv4", "80:tcp", "ipv4:ipv6", "1-", "-1", "any:80", "ipv4:6:80"
    )
  }

  @Test
  fun testDhcpSocketPriorityIsZeroToFifteen() {
    accepted(ConfigParseDhcpSocketPriorityOptionValue(), "0", "6", "15", "0xf")
    rejected(ConfigParseDhcpSocketPriorityOptionValue(), "-1", "16", "2147483647", "-2147483648", "abc")
  }
}
