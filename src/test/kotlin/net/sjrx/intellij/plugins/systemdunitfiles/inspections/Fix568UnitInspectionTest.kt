package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

/**
 * Inspection-level checks for the unit / service / cgroup validators fixed in #568. Every verdict was confirmed with
 * steve-local/systemd-tools/bin/sd-check at 0f0682b773, one value per file, except the [Install] ones, which
 * systemctl (src/shared/install.c) reads and the fuzzer ignores.
 */
class Fix568UnitInspectionTest : AbstractUnitFileTest() {

  private fun highlightTexts(fileName: String, file: String): List<String> {
    setupFileInEditor(fileName, file)
    enableInspection(InvalidValueInspection::class.java)
    return myFixture.doHighlighting().map { it.text }
  }

  fun testEmergencyActionsAreAccepted() {
    // language="unit file (systemd)"
    val file = """
      [Unit]
      FailureAction=halt
      FailureAction=halt-force
      FailureAction=halt-immediate
      FailureAction=kexec
      FailureAction=kexec-force
      FailureAction=soft-reboot
      FailureAction=soft-reboot-force
      JobTimeoutAction=halt-force
      SuccessAction=kexec
      [Service]
      FailureAction=halt
    """.trimIndent()

    assertEmpty(highlightTexts("file.service", file))
  }

  fun testUnitDependencyContinuationLinesAndSpecifiers() {
    // language="unit file (systemd)"
    val file = """
      [Unit]
      Before=before-me.socket \
        before-me.service
      After=some.service.service \
        some@instance.service \
        some@%i.service
      OnFailure=notify-failed-backup@%n
      Wants=a@.service a@b@.service x:y\z.service -.mount
      Requires=foo@%j-%p.service %N.target
    """.trimIndent()

    assertEmpty(highlightTexts("file.service", file))
  }

  fun testUnitDependencyRejectsWhatSystemdRejects() {
    // language="unit file (systemd)"
    val file = """
      [Unit]
      Conflicts=@conflicts@.service
      Wants=foo
      Wants=foo.bar
      Wants=.service
      Wants="b.service"
      After=a%T.service
      After=a@b.mount
      After=a.SERVICE
    """.trimIndent()

    val texts = highlightTexts("file.service", file)
    assertSameElements(texts, "@", "foo", "bar", ".service", "service\"", "%T", "a@b.mount", "SERVICE")
  }

  fun testUnitDependencyLengthLimit() {
    val ok = "a".repeat(246) + ".service"
    val tooLong = "a".repeat(250) + ".service"
    val file = "[Unit]\nWants=$ok\nWants=$tooLong\n"

    assertSameElements(highlightTexts("file.service", file), tooLong)
  }

  fun testInstallKeysStillNeedValidUnitNames() {
    // Also= is refused while systemctl parses the file; WantedBy=/RequiredBy= when the unit is enabled. Instances of
    // types that cannot be templated are only refused by PID 1, so they are not flagged here.
    // language="unit file (systemd)"
    val file = """
      [Install]
      WantedBy=some@instance.service some.socket some.invalid
      RequiredBy=some.service foo@bar.mount
      Also=test3
      Also=a@%i.service
    """.trimIndent()

    assertSameElements(highlightTexts("file.service", file), "invalid", "test3")
  }

  fun testModeAcceptsAnyOctalNumberUpTo7777() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      UMask=71
      UMask=0
      UMask=7777
      UMask=00022
      UMask=10000
      UMask=8
      UMask=+22
      UMask=0x1
    """.trimIndent()

    assertSameElements(highlightTexts("file.service", file), "10000", "8", "+22", "0x1")
  }

  fun testTimeSpanOverflow() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      IPv6RetransmissionTimeSec=18446744073708
      IPv6RetransmissionTimeSec=18446744073709551 us
      IPv6RetransmissionTimeSec=9223372036854775807us
      IPv6RetransmissionTimeSec=584541y
      IPv6RetransmissionTimeSec=999999999999999999
      IPv6RetransmissionTimeSec=18446744073709
      IPv6RetransmissionTimeSec=9223372036854775808us
      IPv6RetransmissionTimeSec=584542y
    """.trimIndent()

    assertSize(4, highlightTexts("file.network", file))
  }

  fun testCoalesceSecMustFitInAUint32() {
    // language="unit file (systemd)"
    val file = """
      [Link]
      RxCoalesceSec=4294s
      RxCoalesceSec=4294.967295s
      RxCoalesceSec=71min
      StatisticsBlockCoalesceSec=1us
      RxCoalesceSec=0
      RxCoalesceSec=infinity
      TxCoalesceSec=2hour
      RxCoalesceSec=4295s
      RxCoalesceSec=4294.967296s
      RxCoalesceSec=72min
      StatisticsBlockCoalesceSec=0
      StatisticsBlockCoalesceSec=0.5us
      CoalescePacketRateSampleIntervalSec=0
    """.trimIndent()

    assertSize(8, highlightTexts("file.link", file))
  }

  fun testCgroupSocketBindAndDisableControllersInAFile() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      SocketBindAllow=ipv4:tcp: 12345
      SocketBindAllow=TCP
      DisableControllers=blkio
      DisableControllers=cpu invalid memory
      NUMAPolicy=preferred
      ExecSearchPath=/tmp:/bin:/usr/bin:%V
      [Unit]
      Description=Ends with a percent %
      RequiresMountsFor=/tmp /var/log %h
      WantsMountsFor=%f
    """.trimIndent()

    assertEmpty(highlightTexts("file.service", file))
  }

  fun testMountWhere() {
    // language="unit file (systemd)"
    val file = """
      [Mount]
      Where=/hoge/with space
      Where=/
      Where=/a/../b
      Where=a
    """.trimIndent()

    assertSize(2, highlightTexts("file.mount", file))
  }

  fun testSocketProtocolAndBindIpv6Only() {
    // language="unit file (systemd)"
    val file = """
      [Socket]
      SocketProtocol=mptcp
      SocketProtocol=262
      BindIPv6Only=false
      BindIPv6Only=yes
      SocketProtocol=tcp
      BindIPv6Only=Both
    """.trimIndent()

    assertSameElements(highlightTexts("file.socket", file), "tcp", "Both")
  }

  fun testTimeSpanNumbersAsParseTimeReadsThem() {
    // strtoll() takes a leading `+`; `.5` has no integer part; a unit-less number must be followed by whitespace
    // (time-util.c#L1234-L1255). All verdicts confirmed with sd-check on RestartSec=.
    // language="unit file (systemd)"
    val file = """
      [Service]
      RestartSec=+5
      RestartSec=+5.5
      RestartSec=+5min +3s
      RestartSec=.5
      RestartSec=12.34 .56
      RestartSec=12.34s.56
      RestartSec=5 6
      RestartSec=12.34.56
      RestartSec=1.5.5s
      RestartSec=+.5
      RestartSec=5.
      RestartSec=++5
    """.trimIndent()

    assertSize(5, highlightTexts("file.service", file))
  }

  fun testCoalesceSecUsesParseTime() {
    // `+5` and `.5` are fine for parse_sec(); `999999999999999999` overflows parse_sec() itself.
    // language="unit file (systemd)"
    val file = """
      [Link]
      RxCoalesceSec=+5
      RxCoalesceSec=.5
      RxCoalesceSec=4294967295us
      RxCoalesceSec=999999999999999999
      RxCoalesceSec=9223372036854775808
      RxCoalesceSec=12.34.56
      RxCoalesceSec=4294967296us
    """.trimIndent()

    assertSize(4, highlightTexts("file.link", file))
  }

  fun testSocketBindPortRangeMustNotEndBelowItsStart() {
    // parse_ip_port_range() refuses h < l (parse-util.c#L753-L754).
    // language="unit file (systemd)"
    val file = """
      [Service]
      SocketBindDeny=0--0
      SocketBindDeny=ipv6:80-80
      SocketBindDeny=0x10-16
      SocketBindDeny=5-1
      SocketBindDeny=ipv4:tcp:90-80
      SocketBindDeny=0x10-15
      SocketBindAllow=tcp:2-1
      SocketBindAllow=1--0
    """.trimIndent()

    assertSize(5, highlightTexts("file.service", file))
  }
}
