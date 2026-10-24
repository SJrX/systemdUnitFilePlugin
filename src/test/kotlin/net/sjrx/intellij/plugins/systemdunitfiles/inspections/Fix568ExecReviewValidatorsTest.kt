package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import com.intellij.lang.annotation.HighlightSeverity
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

/**
 * Follow-up to [Fix568ExecValidatorsTest] from the review of GitHub #568: unknown specifiers, IO limit device paths,
 * memory limit percentages and zero, case-insensitive errno names, CPU ranges, quoted namespace paths and duplicate
 * image policy rules. Every verdict was confirmed with steve-local/systemd-tools/bin/sd-check at systemd 0f0682b773,
 * one value per file.
 */
class Fix568ExecReviewValidatorsTest : AbstractUnitFileTest() {

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

  fun testExecSpecifiers() {
    // unit_path_printf() / unit_full_printf() refuse an unknown specifier in the path or any argument; "%%" is a
    // literal '%', so "%%/bin/x" is neither a file name nor an absolute path (while "%%" is the file name "%").
    assertVerdicts(
      "file.service", "Service", "ExecStart",
      valid = listOf(
        "/bin/x %%Q", "/bin/x %-", "/bin/x 100%", "/bin/x %", "/bin/x%%y", "/bin/x%", "%%x", "%-", "%h", "/bin/%h", "/bin/x %h", "%n",
        "|echo %%Q", "/bin/x ; /bin/y %h", "%%",
      ),
      invalid = listOf(
        "/bin/x %Q", "/bin/x %%%Q", "%Q", "/bin/%Q", "-%Q", "/bin/x \"%Q\"", "/bin/x \\%Q", "/bin/x a%Qb", "@/bin/x %Q", "|echo hi %Q",
        "/bin/x ; /bin/y %Q", "%%/bin/x", "/bin/x%%/", "/bin/x%%*",
      ),
    )
  }

  fun testPathSpecifiers() {
    assertVerdicts(
      "file.service", "Service", "WorkingDirectory",
      valid = listOf("%h", "-%h", "%h/a", "/a/%n/b", "/a%-", "/a%", "/a%%", "~"),
      invalid = listOf("%Q", "/a/%Q", "%h%Q", "%%h", "%%", "-%%", "/a/%%/../b"),
    )
    assertVerdicts(
      "file.service", "Service", "EnvironmentFile",
      valid = listOf("%h", "-%h", "/a/%n/b", "/a%%"),
      invalid = listOf("%Q", "/a/%Q", "%%h", "-%%"),
    )
    assertVerdicts(
      "file.service", "Service", "StandardOutput",
      valid = listOf("fd:%n", "fd:a%%b", "fd:%", "fd:%-", "file:/a%%b", "file:%h/a", "truncate:/a%-"),
      invalid = listOf("fd:%Q", "file:%Q", "file:/a/%Q", "append:%%", "file:/%%/.."),
    )
    assertVerdicts(
      "file.service", "Service", "StandardInput",
      valid = listOf("fd:%n", "fd:a%%b", "file:%h/a", "file:/a/%h"),
      invalid = listOf("fd:%Q", "file:%%", "file:%Q", "file:/a/%Q"),
    )
  }

  fun testUserAndUnsetEnvironmentSpecifiers() {
    assertVerdicts("file.service", "Service", "User", listOf("%u", "%%"), listOf("%Q"))
    assertVerdicts("file.service", "Service", "UnsetEnvironment", listOf("X%b", "FOO=%h", "FOO"), listOf("%Q", "X%Q", "FOO=%Q"))
  }

  fun testIOLimitDevicePaths() {
    // The device word is unquoted (a backslash keeps the next character) and must survive path_simplify() without
    // a ".." component; it does not have to be absolute.
    assertVerdicts(
      "file.service", "Service", "IOReadBandwidthMax",
      valid = listOf(
        "/dev/sda\\ x 1M", "\"/dev/a\\\"b\" 1M", "'/dev/a b' 1M", "/../a 1M", "/./a 1M", "a 1M", "a/./b 1M", "/ 1M", "// 1M",
        "/dev/sd\\a 1M", "/dev/%n 1M",
      ),
      invalid = listOf(
        "/dev/../x 1M", "/a/../b 1M", "a/../b 1M", ".. 1M", ". 1M", "./ 1M", "\"\" 1M", "a/.. 1M", "../a 1M", "%Q 1M", "/dev/%Q 1M",
        "\"/dev/sda 1M",
      ),
    )
  }

  fun testMemoryLimitPercentagesAndZero() {
    // parse_permyriad() takes "N‱", "N[.d]‰" or "N[.d[d]]%" (safe_atoi(), so "+50%", " 50%" and "0x10%" are fine)
    // up to 100%; 0 is only allowed for MemorySwapMax=, MemoryZSwapMax=, MemoryLow=, MemoryMin= and StartupMemoryLow=.
    assertVerdicts(
      "file.service", "Service", "MemoryMax",
      valid = listOf(
        "1‱", "10000‱", "1.5‰", "1000‰", "50%", "50.5%", "50.55%", "100%", "100.0%", " 50%", "+50%", "0x10%", "010%", "0.01%", "1G",
        "infinity",
      ),
      invalid = listOf(
        "0", "0%", "0.0%", "0‱", "0‰", "0K", "0B", "10001‱", "1.55‰", "1000.1‰", "50.%", "50.555%", "100.5%", "101%", "1 %", "-0%",
        "-1%", ".5%",
      ),
    )
    assertVerdicts("file.service", "Service", "MemoryHigh", listOf("1%"), listOf("0", "0%"))
    assertVerdicts("file.service", "Service", "StartupMemoryMax", emptyList(), listOf("0", "0%"))
    assertVerdicts("file.service", "Service", "MemoryLow", listOf("0", "0%", "1G"), listOf("50 %"))
    assertVerdicts("file.service", "Service", "MemoryMin", listOf("0", "0%"), emptyList())
    assertVerdicts("file.service", "Service", "MemorySwapMax", listOf("0", "0%"), emptyList())
    assertVerdicts("file.service", "Service", "MemoryZSwapMax", listOf("0"), emptyList())
    assertVerdicts("file.service", "Service", "StartupMemoryLow", listOf("0"), emptyList())
  }

  fun testSystemCallErrorNumberNamesIgnoreCase() {
    // errno_from_name() is a case-insensitive gperf lookup, and E2BIG has a digit second.
    assertVerdicts(
      "file.service", "Service", "SystemCallErrorNumber",
      valid = listOf("EPERM", "eperm", "Eperm", "eDOM", "E2BIG", "e2big", "kill"),
      invalid = listOf("KILL", "0"),
    )
  }

  fun testCPUAffinityRanges() {
    // parse_range(): safe_atou() on each side (base 0, '+' allowed, "-0" is 0), lower <= upper, below 8192.
    assertVerdicts(
      "file.service", "Service", "CPUAffinity",
      valid = listOf(
        "\"0-3\"", "'0'", "0x0-0x3", "010", "1-0x2", "+1", ",", " , ", "0x1", "8191", "0-8191", "%i", "0--0", "0 , 1", ", 0 ,",
      ),
      invalid = listOf(
        "0-1-2", "-1-3", "1-", "1,-", "4294967295", "4294967296", "0-4294967295", "3-0", "8192", "0-8192", "%Q", "0 %Q", "%%", "1%%",
        "1--0", "\"0", "\"0 1\"",
      ),
    )
  }

  fun testNamespacePathsQuotesSpecifiersAndDotDot() {
    assertVerdicts(
      "file.service", "Service", "ReadOnlyPaths",
      valid = listOf(
        "\"/a b\"", "%h", "-%h", "+%h", "-+%h", "\"-/a b\"", "'/a' /b", "-+/a", "/a\\ b", "/../a", "/a/./b", "/a//b",
      ),
      invalid = listOf("%Q", "/a/%Q", "/a/../b", "/a /b/../c", "%%/a", "+-/a", "a", "-a"),
    )
  }

  fun testLimitNiceRawFormAfterWhitespace() {
    // rlimit_parse_nice() only looks at the first character of a field, so " +5" after the ':' is the raw limit 5
    // (safe_atou64() skips the whitespace and takes the sign). The first review pass listed "+0: +5" as a strictness
    // corner, but systemd refuses that one too (soft 20 > hard 5); the others below were wrongly flagged.
    assertVerdicts(
      "file.service", "Service", "LimitNICE",
      valid = listOf("0: +5", "0: -0", "0: +0x5", "1: +40", "0:  +5", "+5: 30", "-5: 30"),
      invalid = listOf("+0: +5", "0: +41", "0: + 5", "0: -5", "+5 :10", "5: +0"),
    )
  }

  fun testRootImagePolicyDuplicateRules() {
    assertVerdicts(
      "file.service", "Service", "RootImagePolicy",
      valid = listOf("foo=verity:foo=signed", "root=verity:usr=signed", "root=verity:root-verity=signed"),
      invalid = listOf("root=verity:root=signed", "root=verity: root =signed", "=verity:=signed", "=verity: =signed"),
    )
  }
}
