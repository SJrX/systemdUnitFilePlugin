package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import com.intellij.lang.annotation.HighlightSeverity
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

/**
 * Exec / sandboxing validators checked against systemd's own parsers (GitHub #568). Every verdict below was
 * confirmed with steve-local/systemd-tools/bin/sd-check at systemd 0f0682b773, one value per file.
 *
 * Weak warnings (e.g. the "use an absolute path" hint on Exec*= lines, or a deprecated address family) do not count
 * as flagging a value.
 */
class Fix568ExecValidatorsTest : AbstractUnitFileTest() {

  /**
   * Puts every value on its own line under [section] in [fileName] and checks that exactly the [invalid] ones are
   * flagged (at WARNING or above) by the InvalidValue inspection.
   */
  private fun assertVerdicts(fileName: String, section: String, key: String, valid: List<String>, invalid: List<String>) {
    val values = valid + invalid
    val text = "[$section]\n" + values.joinToString("") { "$key=$it\n" }
    setupFileInEditor(fileName, text)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting().filter { it.severity >= HighlightSeverity.WARNING }
    val document = myFixture.editor.document
    val flaggedLines = highlights.map { document.getLineNumber(it.startOffset) }.toSet()

    val wronglyFlagged = valid.filterIndexed { i, _ -> (i + 1) in flaggedLines }
    val wronglyAccepted = invalid.filterIndexed { i, _ -> (valid.size + i + 1) !in flaggedLines }
    assertTrue(
      "$key: flagged although systemd accepts them: $wronglyFlagged; accepted although systemd rejects them: $wronglyAccepted",
      wronglyFlagged.isEmpty() && wronglyAccepted.isEmpty()
    )
  }

  fun testExecStart() {
    assertVerdicts(
      "file.service", "Service", "ExecStart",
      valid = listOf(
        "/bin/x arg",
        "!!/sbin/x",
        "@/bin/x x",
        "-/bin/x",
        "/bin/x ; /bin/y",
        "/bin/x \\; a",
        "|",
        "%h/bin/x",
        "/a/../b",
        "foo",
        // An unknown escape sequence is only a warning; systemd keeps the backslash.
        "bash -x -c '! capsh --print | sed -re \"s/[^ ]+?\\-[epi]+//g\" -e '/IAB/d' | grep cap_mknod'",
        "cmd \\' \\\" \\\\ \\a \\b \\f \\n \\r \\s \\t \\v \\x0A \\012 \\u1234 \\U12abcdef",
      ),
      invalid = listOf(
        "@ /some/cmd arg",
        "@ -/some/cmd arg",
        "@/sbin/docker-compose",
        "@docker-compose",
        "@-:!!/sbin/docker-compose",
        "@service_path@",
        "+!/some/cmd arg",
        "@!!+/some/cmd arg",
        "@+@/some/cmd arg",
        "@SBIN_DIR@/haveged -w 1024 -v 1 --Foreground -v 64",
        "-@SBIN_DIR@/haveged -c root=/sysroot",
        "scripts/run-client-core",
        "\${CMAKE_INSTALL_FULL_BINDIR}/gammu-smsd --pid=/run/gammu-smsd.pid --daemon",
        "__BINDIR__/purgeLocalCache",
        "MISCDIR/mdcheck --restart",
        "ExecReload=/bin/sh -c 'case \"\$START_\" in (NO|no) ;; (*) /usr/sbin/gapd ;; esac'",
        "/usr/bin/fo*",
        "/usr/bin/",
        ".",
        "/bin/x \"unbal",
        "/bin/x ; @/bin/y",
      ),
    )
  }

  fun testExecStopAndFriendsShareTheExecParser() {
    assertVerdicts("file.service", "Service", "ExecStop", listOf("/bin/stop"), listOf("@DIR@/softether/vpnbridge/vpnbridge stop"))
    assertVerdicts("file.service", "Service", "ExecStopPost", listOf("-/bin/stop"), listOf("@docker-compose", "@-:!!docker-compose"))
    assertVerdicts("file.service", "Service", "ExecStartPre", listOf("/bin/pre"), listOf("TARGET/systemd/incus-agent-setup"))
    assertVerdicts("file.service", "Service", "ExecReload", listOf("/bin/kill -HUP \$MAINPID"), listOf("__ASTERISK_SBIN_DIR__/asterisk -rx 'core reload'"))
  }

  fun testWorkingDirectory() {
    assertVerdicts(
      "file.service", "Service", "WorkingDirectory",
      valid = listOf("%S/osmocom", "%h", "%E/wireguard", "/some/absolute/path /some/other/path", "/some/absolute/path ~", "-~", "~", "/..", "/a/./b", "//a"),
      invalid = listOf("/a/../b", "/a/..", "~/x", "- /a", "relative/path", "--/path"),
    )
  }

  fun testEnvironmentFile() {
    assertVerdicts(
      "file.service", "Service", "EnvironmentFile",
      valid = listOf("%E/wireguard/%i.env", "-%E/yubikey-touch-detector/service.conf", "-%h/.config/gpu-screen-recorder.env", "/etc/a b.env"),
      invalid = listOf("relative.env", "-relative", "-/a/../b"),
    )
  }

  fun testIOSchedulingClass() {
    assertVerdicts(
      "file.service", "Service", "IOSchedulingClass",
      valid = listOf("0", "1", "2", "3", "4", "7", "0x7", "none", "realtime", "best-effort", "idle"),
      invalid = listOf("8", "-1", "Idle", "foo"),
    )
  }

  fun testCPUSchedulingPolicy() {
    // 2147483647 only draws "Unsupported CPU scheduling policy", which does not reject it.
    assertVerdicts(
      "file.service", "Service", "CPUSchedulingPolicy",
      valid = listOf("ext", "other", "rr", "5", "2147483647"),
      invalid = listOf("2147483648", "-1", "foo"),
    )
  }

  fun testIOLimits() {
    assertVerdicts(
      "file.service", "Service", "IOReadIOPSMax",
      valid = listOf(
        "/dev/disk/by-path/pci-0000:00:1f.2-scsi-0:0:0:0 123",
        "/dev/disk/by-path/pci-0000:00:1f.2-scsi-0:0:0:0 12 G",
        "/dev/disk/by-path/pci-0000:00:1f.2-scsi-0:0:0:0  10M",
        "/dev/disk/by-path/pci-0000:00:1f.2-scsi-0:0:0:0 12E",
        "/dev/disk/by-path/pci-0000:00:1f.2-scsi-0:0:0:0 1K",
        "/dev/sda 1.5K",
        "/dev/sda 1G 5M",
        "/dev/sda infinity",
        "/dev/sda +5",
        "foo 10M",
        "\"/dev/a b\" 5M",
        "%t/x 5",
      ),
      invalid = listOf("/dev/sda 0", "/dev/sda 10M 20M", "/dev/sda 5k", "/dev/sda"),
    )
    assertVerdicts("file.service", "Service", "IOWriteBandwidthMax", listOf("/dev/disk/by-path/pci-0000:00:1f.2-scsi-0:0:0:0 5M", "/dev/sda 1E"), listOf("/dev/sda 0"))
  }

  fun testKillSignal() {
    assertVerdicts(
      "file.service", "Service", "KillSignal",
      valid = listOf("SIGRTMIN", "RTMIN", "RTMIN+3", "SIGRTMAX-2", "RTMAX", "RTMIN+30", "RTMAX-30", "RTMIN+0x2", "RTMAX-0", "64", "+5", "0x9", "HUP", "SIGTERM"),
      invalid = listOf(
        "SIGCLD", "SIGEMT", "SIGINFO", "SIGIOT", "SIGLOST", "SIGPOLL", "0", "65", "-1", "RTMIN+31", "RTMAX-31", "RTMIN+-0", "RTMIN+ 3",
        "RTMIN3", "hup", "SIGhup", "SIGSIGHUP",
      ),
    )
  }

  fun testCapabilitySetsAcceptUnknownNames() {
    // capability_set_from_string() skips names it does not know, so systemd never rejects a capability set.
    assertVerdicts(
      "file.service", "Service", "CapabilityBoundingSet",
      valid = listOf(
        "CAP_NET_ADMIN CAP_NET_BIND_SERVICE CAP_NET_RAW CAP_SETGID CAP_SETUID CAP_CHOWN CAP_DAC_OVERRIDE LimitNPROC=64",
        "~ CAP_SYS_ADMIN CAP_DAC_OVERRIDE   CAP_SYS_PTRACE  CAP_NET_ADMIN  CAP_SYS_BOOT",
        "CAP_AUDIT_CONTROLCAP_SETGID",
        "CAP_CHOWNER",
        "CAP_NET_ADMIN CAP_CHOW",
        "CAP_SYS_BOOT ~CAP_SYS_TIME CAP_SYSLOG",
        "HELLO",
        "~CAP_BLOCK_SUSPEND CAP_SYS_BOOT CAP_SYS_RAWIO CAP_AUDIT_* CAP_SYS_PTRACE",
        "~CAP_SYS_BPF CAP_NET_ADMIN",
        "~",
        "cap_chown",
        "63",
      ),
      invalid = emptyList(),
    )
    assertVerdicts("file.service", "Service", "AmbientCapabilities", listOf("~ CAP_NET_ADMIN CAP_NET_RAW  CAP_WAKE_ALARM"), emptyList())
  }

  fun testStandardOutputAndError() {
    assertVerdicts(
      "file.service", "Service", "StandardOutput",
      valid = listOf(
        "syslog", "syslog+console", "fd", "fd:", "fd:a b", "file", "append", "broker", "broker-log", "file:%t/x", "file:/a/./b",
        "append:/var/log/x", "truncate:/a",
      ),
      invalid = listOf(
        "append: /some/relative/path", "append:some/relative/path", "file: /some/relative/path", "file:some/relative/path", "file:",
        "fd:a:b", "file:/a/../b", "foo",
      ),
    )
    assertVerdicts("file.service", "Service", "StandardError", listOf("syslog", "syslog+console"), listOf("foo"))
  }

  fun testStandardInput() {
    assertVerdicts(
      "file.service", "Service", "StandardInput",
      valid = listOf("fd", "file", "broker", "data", "tty-force", "fd:", "file:/some/absolute/path socket"),
      invalid = listOf("file:", "file:rel", "fd:a:b", "foo"),
    )
  }

  fun testNiceAndOOMScoreAdjust() {
    assertVerdicts("file.service", "Service", "Nice", listOf("+12", "+1", "+19", "-20", "0x5"), listOf("20", "-21", "+20", "abc"))
    assertVerdicts("file.service", "Service", "OOMScoreAdjust", listOf("+1000", "+321", "-1000"), listOf("1001", "+1001"))
  }

  fun testRestrictAddressFamilies() {
    assertVerdicts(
      "file.service", "Service", "RestrictAddressFamilies",
      valid = listOf(
        "~ AF_INET AF_VSOCK", "~ AF_XDP AF_LOCAL  AF_SECURITY", "AF_DECNET", "AF_INET, AF_INET6", "AF_INET,AF_INET6", ",AF_INET,", "inet",
        "INET", "af_inet", "~", "none", "\"AF_INET\"",
      ),
      invalid = listOf("none AF_INET", "AF_UNSPEC", "AF_BOGUS", "AF_INET \"AF_UNIX"),
    )
  }

  fun testRlimits() {
    assertVerdicts(
      "file.service", "Service", "LimitCPU",
      valid = listOf("10s 15ms : infinity", "15s 10us : 20s 5ms", "infinity : infinity", "1.9s:1.5s", "6m"),
      invalid = listOf("2s:1s", "infinity:5", "5x"),
    )
    assertVerdicts(
      "file.service", "Service", "LimitNICE",
      valid = listOf("+19", "-20", "40", "+0", "-0", "0", "+19:+0", "++5"),
      invalid = listOf("+20", "-10:+10", "-21", "41", "+0:+19"),
    )
    assertVerdicts(
      "file.service", "Service", "LimitNOFILE",
      valid = listOf("1: 2", "0x10", "+5", "infinity:infinity", "18446744073709551614"),
      invalid = listOf("1: infinity", "1 :2", "5:3", "18446744073709551615"),
    )
    assertVerdicts("file.service", "Service", "LimitRTTIME", listOf("12us : 15", "20000000000000"), listOf("15 : 12us"))
    assertVerdicts(
      "file.service", "Service", "LimitFSIZE",
      valid = listOf("4294967296", "1 K: 2 G", "12 G: 24 G", "1G 512M", "18446744073709551614", "1K:infinity"),
      invalid = listOf("18446744073709551615", "16E", "2K:1K", "infinity:1K", "1:", "1:2:3", "12 G : 24 G", "12 G :24 G"),
    )
  }

  fun testRootImagePolicy() {
    // Unknown designators and flags are skipped in graceful mode, so they are fine.
    assertVerdicts(
      "file.service", "Service", "RootImagePolicy",
      valid = listOf(
        "home=encrypted+absent+", "home=encrypted+used", "home=encrypted:root=encrypted+used", "opt=unprotected", "root=unsigned",
        "=verity", "root=verity:=signed", "-", "*", "~", "root=encryptedwithintegrity+ext4",
      ),
      invalid = listOf("+verity", "root", ":", "root=verity:"),
    )
  }

  fun testMemoryLimits() {
    assertVerdicts("file.service", "Service", "MemoryMin", listOf("12 E", "12P", "1G 512M", "1.5K", "10%"), listOf("16E", "5k", "abc"))
    assertVerdicts("file.service", "Service", "MemorySwapMax", listOf("12 E", "12P"), emptyList())
  }

  fun testSecureBitsAcceptUnknownWords() {
    // secure_bits_from_string() skips words it does not know.
    assertVerdicts("file.service", "Service", "SecureBits", listOf("invalid", "invalid_value", "keep-caps invalid_value", "noroot", "exec-restrict-file"), emptyList())
  }

  fun testSystemCallErrorNumber() {
    assertVerdicts("file.service", "Service", "SystemCallErrorNumber", listOf("1", "4095", "0x10", "EPERM", "kill"), listOf("0", "4096", "-1"))
  }

  fun testStateDirectoryQuota() {
    assertVerdicts("file.service", "Service", "StateDirectoryQuota", listOf("1P", "4294967296", "5.52%", "off", "100%", "10G"), listOf("101%", "5.123%", "abc"))
  }

  fun testUser() {
    assertVerdicts(
      "file.service", "Service", "User",
      valid = listOf("65534", "0", "4294967294", "a b", "-a", "1a", "+5", "root", "%i"),
      invalid = listOf("-1000", "-", ".", "..", "65535", "0123", "4294967295"),
    )
  }

  fun testUnsetEnvironment() {
    assertVerdicts("file.service", "Service", "UnsetEnvironment", listOf("QUUX=waldo VAR3=somethingelse VAR4 X%b=%U"), listOf("1ABC"))
  }

  fun testPrivateUsersInAService() {
    assertVerdicts("file.service", "Service", "PrivateUsers", listOf("full", "self", "managed", "identity", "yes"), emptyList())
  }

  fun testRestrictNamespaces() {
    assertVerdicts("file.service", "Service", "RestrictNamespaces", listOf("~ cgroup pid  uts", "~", "yes", "net"), listOf("~ foo", "foo"))
  }

  fun testSELinuxContextAndSmackProcessLabel() {
    // A '%' that is not followed by an alphanumeric character is kept as it is. "%e" is an unknown specifier, which
    // systemd refuses when it is built with SELinux support.
    assertVerdicts("file.service", "Service", "SELinuxContext", listOf("ends with %", "a%-b"), listOf("bad %e specifier"))
    assertVerdicts("file.service", "Service", "SmackProcessLabel", listOf("ends with %", "a%-b"), listOf("%Z"))
  }

  fun testManagedOOMMemoryPressureDurationSec() {
    assertVerdicts("file.service", "Service", "ManagedOOMMemoryPressureDurationSec", listOf("1s", "2min"), listOf("infinity", "500ms"))
  }

  fun testCPUAffinity() {
    assertVerdicts("file.service", "Service", "CPUAffinity", listOf("%I", "0 1, 2 ,  3, 4-8", "numa"), listOf("a"))
  }

  fun testPacketLimitsTakeTheFullUnsignedRange() {
    assertVerdicts("file.network", "NetworkEmulator", "PacketLimit", listOf("4294967295", "0"), listOf("4294967296"))
    assertVerdicts("file.network", "HeavyHitterFilter", "PacketLimit", listOf("4294967295", "0"), listOf("4294967296"))
  }

  fun testReadWritePathsAcrossALineContinuation() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      ReadWritePaths=+/some/path-+/ -/some/other/path \
        -+/one/more/path
      """.trimIndent()
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    assertEmpty(myFixture.doHighlighting().filter { it.severity >= HighlightSeverity.WARNING })
  }
}
