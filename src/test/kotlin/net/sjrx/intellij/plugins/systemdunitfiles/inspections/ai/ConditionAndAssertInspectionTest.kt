package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/*
 * Expectations here are derived from systemd's C parsers at a8e93919c3 (https://github.com/systemd/systemd/blob/a8e93919c3),
 * the commit systemd-build/build/last_commit_hash pins, and NOT from what happens to appear in
 * real-world unit files. Where a case is subtle the individual test says which routine decides it.
 *
 * Several of the rejection cases are lifted from systemd's own negative fixtures under
 * test/test-network/conf/ and from KDE's syntax-highlighting test input, both of which deliberately
 * contain malformed values.
 */

/**
 * Tests for the `[Unit] Condition…=` / `Assert…=` validators (#509).
 *
 * Every case here is checked against systemd's own parsers: config_parse_unit_condition_path and
 * config_parse_unit_condition_string in src/core/load-fragment.c, plus the per-type checks in
 * src/shared/condition.c.
 */
class ConditionAndAssertInspectionTest : AbstractUnitFileTest() {

  private fun highlights(text: String): Int {
    setupFileInEditor("f.service", text)
    enableInspection(InvalidValueInspection::class.java)
    return myFixture.doHighlighting().size
  }

  private fun assertAccepted(vararg lines: String) =
    assertEquals(lines.joinToString(), 0, highlights("[Unit]\n" + lines.joinToString("\n") + "\n"))

  // The classic engine can split a partial match into more than one region, so require "at least one".
  private fun assertRejected(line: String) =
    assertTrue(line, highlights("[Unit]\n$line\n") >= 1)

  // ------------------------------------------------------------------ path conditions

  @Test
  fun testPathConditionsAcceptAbsolutePathsAndMarkers() {
    assertAccepted(
      "ConditionPathExists=/etc/foo.conf",
      "ConditionPathExists=!/var/cache/seeded",
      "ConditionPathExists=|/etc/optional.conf",
      "ConditionPathExists=|!/etc/ld.so.cache",
      "ConditionPathExistsGlob=/dev/snd/control*",
      "ConditionDirectoryNotEmpty=/var/lib/dracut/hooks",
      "ConditionFileNotEmpty=/etc/machine-id",
      "ConditionFileIsExecutable=/usr/bin/foo",
      "ConditionPathIsReadWrite=/proc/sys",
      "ConditionPathIsDirectory=/run",
      "ConditionPathIsSymbolicLink=/etc/localtime",
      "ConditionPathIsMountPoint=/boot",
      "ConditionPathIsEncrypted=/home",
      "ConditionPathIsSocket=/run/dbus/system_bus_socket",
      "ConditionNeedsUpdate=/etc",
    )
  }

  @Test
  fun testPathConditionsCoverTheAssertTwins() {
    // The wildcard registration has to reach the [Unit] Assert… keys as well.
    assertAccepted(
      "AssertPathExists=/etc/foo.conf",
      "AssertFileIsExecutable=!/usr/bin/foo",
      "AssertDirectoryNotEmpty=|/var/lib/foo",
    )
  }

  @Test
  fun testPathConditionsAcceptSpecifiers() {
    // unit_path_printf() runs before the absolute-path check, so a value may start with a specifier.
    assertAccepted(
      "ConditionPathExists=%t/gnome-shell-disable-extensions",
      "ConditionPathExists=%h/.config/foo",
      "ConditionPathExistsGlob=%C/drkonqi/sentry-envelopes/*",
      "ConditionFileNotEmpty=/etc/conmux/%i",
    )
  }

  @Test
  fun testPathConditionsRejectRelativePaths() {
    // path_simplify_and_warn(..., PATH_CHECK_ABSOLUTE) rejects anything that isn't absolute.
    assertRejected("ConditionPathExists=etc/foo.conf")
    assertRejected("ConditionPathExists=../foo")
  }

  @Test
  fun testPathConditionsRejectWhitespaceAfterMarkers() {
    // config_parse_unit_condition_path advances past '|' and '!' with a bare rvalue++, so a space
    // after a marker becomes part of the path and the absolute check then fails.
    assertRejected("ConditionPathExists=|! /some/path")
    assertRejected("ConditionPathExists=! /some/path")
  }

  @Test
  fun testPathConditionsRejectMarkersInTheWrongOrder() {
    // '|' is only recognised first; in "!|/x" the '!' negates and the parameter is literally "|/x".
    assertRejected("ConditionPathExists=!|/some/path")
    assertRejected("ConditionPathExists=!!/some/path")
  }

  @Test
  fun testPathConditionsTakeTheWholeValueAsOnePath() {
    // The value is never split on whitespace and never unescaped, so a space is just a path
    // character: `/etc /var` is one directory whose name contains a space, not two paths.
    assertAccepted(
      "ConditionNeedsUpdate=/etc /var",
      "ConditionPathExists=/mnt/My Data/.stamp",
      "ConditionPathExists=!/srv/My Share/spool",
    )
  }

  @Test
  fun testPathConditionsRejectUnnormalizedPaths() {
    // path_simplify() collapses `.` and `//` but leaves `..`, which path_is_normalized() then refuses.
    assertRejected("ConditionPathExists=/etc/../var")
    assertRejected("ConditionPathExists=/etc/..")
  }

  // ------------------------------------------------------------------ ConditionArchitecture

  @Test
  fun testArchitectureAcceptsTableNamesAndNative() {
    assertAccepted(
      "ConditionArchitecture=x86-64",
      "ConditionArchitecture=x86",
      "ConditionArchitecture=arm64-be",
      "ConditionArchitecture=loongarch64",
      "ConditionArchitecture=riscv64",
      "ConditionArchitecture=native",
      "AssertArchitecture=!alpha",
      "ConditionArchitecture=|! ppc64-le",
    )
  }

  @Test
  fun testArchitectureMatchesTheLongestNameNotThePrefix() {
    // Several table entries are prefixes of others (ppc64 / ppc64-le, arm64 / arm64-be, mips / mips64).
    // FlexibleLiteralChoiceTerminal sorts its choices longest-first in its init block, so the order the
    // names are written in below is cosmetic and the classic engine can't stop on a short prefix.
    assertAccepted(
      "ConditionArchitecture=ppc64",
      "ConditionArchitecture=ppc64-le",
      "ConditionArchitecture=arm64",
      "ConditionArchitecture=arm64-be",
      "ConditionArchitecture=mips",
      "ConditionArchitecture=mips64",
      "ConditionArchitecture=mips64-le",
      "ConditionArchitecture=arc",
      "ConditionArchitecture=arc-be",
    )
  }

  @Test
  fun testArchitectureRejectsUnknownAndLists() {
    assertRejected("ConditionArchitecture=x86_64")   // the table spells it with a hyphen
    assertRejected("ConditionArchitecture=invalid")
    assertRejected("ConditionArchitecture=sparc x86")
    assertRejected("ConditionArchitecture=!| alpha")
  }

  // ------------------------------------------------------------------ ConditionVirtualization

  @Test
  fun testVirtualizationAcceptsBooleansCategoriesAndIds() {
    assertAccepted(
      "ConditionVirtualization=no",
      "ConditionVirtualization=yes",
      "ConditionVirtualization=false",
      "ConditionVirtualization=vm",
      "ConditionVirtualization=container",
      "ConditionVirtualization=!container",
      "ConditionVirtualization=!private-users",
      "ConditionVirtualization=microsoft",
      "ConditionVirtualization=systemd-nspawn",
      "ConditionVirtualization=lxc-libvirt",
      "ConditionVirtualization=|vmware",
    )
  }

  @Test
  fun testVirtualizationBooleanDoesNotShadowTheNames() {
    // BOOLEAN is its own alternative here, and it matches a prefix -- so it must be tried after the
    // names, or "none" would be read as the boolean "no" followed by a stray "ne".
    assertAccepted(
      "ConditionVirtualization=none",
      "ConditionVirtualization=no",
      "ConditionVirtualization=n",
      "ConditionVirtualization=off",
      "ConditionVirtualization=openvz",
      "ConditionVirtualization=t",
      "ConditionVirtualization=1",
    )
  }

  @Test
  fun testVirtualizationRejectsUnknownAndLists() {
    assertRejected("ConditionVirtualization=invalid")
    assertRejected("ConditionVirtualization=xen vmware")
  }

  // ------------------------------------------------------------------ ConditionSecurity

  @Test
  fun testSecurityAcceptsTheKnownTechnologies() {
    assertAccepted(
      "ConditionSecurity=selinux",
      "ConditionSecurity=!selinux",
      "ConditionSecurity=apparmor",
      "ConditionSecurity=smack",
      "ConditionSecurity=audit",
      "ConditionSecurity=ima",
      "ConditionSecurity=tomoyo",
      "ConditionSecurity=uefi-secureboot",
      "ConditionSecurity=tpm2",
      "ConditionSecurity=cvm",
      "ConditionSecurity=measured-uki",
      "ConditionSecurity=measured-os",
      "AssertSecurity=| ! selinux",
    )
  }

  @Test
  fun testSecurityRejectsUnknownAndLists() {
    assertRejected("ConditionSecurity=invalid")
    // condition_test_security compares the whole parameter, so this asks for one oddly-named
    // technology rather than for either of two.
    assertRejected("ConditionSecurity=apparmor selinux")
  }

  // ------------------------------------------------------------------ ConditionCapability

  @Test
  fun testCapabilityAcceptsOneNameOrNumber() {
    assertAccepted(
      "ConditionCapability=CAP_SYS_ADMIN",
      "ConditionCapability=!CAP_NET_ADMIN",
      "ConditionCapability=CAP_CHECKPOINT_RESTORE",
      "ConditionCapability=0",
      "ConditionCapability=62",
      "AssertCapability=|! CAP_CHOWN",
    )
  }

  @Test
  fun testCapabilityNamesAreCaseInsensitive() {
    // capability_from_name() looks the name up in a gperf table built with --ignore-case, and
    // capability_to_name() actually renders the canonical form in lower case.
    assertAccepted(
      "ConditionCapability=cap_sys_admin",
      "ConditionCapability=Cap_Net_Admin",
      "AssertCapability=!cap_chown",
    )
  }

  @Test
  fun testCapabilityNumberAcceptsEveryBase() {
    assertAccepted("ConditionCapability=0x1e", "ConditionCapability=016")
    assertRejected("ConditionCapability=0x3F")
  }

  @Test
  fun testCapabilityRejectsUnknownOutOfRangeAndLists() {
    assertRejected("ConditionCapability=CAP_BOGUS")
    assertRejected("ConditionCapability=63")                          // CAP_LIMIT is 62
    assertRejected("ConditionCapability=CAP_NET_ADMIN CAP_NET_RAW")   // one capability only
  }

  // ------------------------------------------------------------------ ConditionControlGroupController

  @Test
  fun testControlGroupControllerAcceptsControllerNames() {
    // Previously mapped onto the boolean grammar, which rejected all of these.
    assertAccepted(
      "ConditionControlGroupController=cpu",
      "ConditionControlGroupController=memory",
      "ConditionControlGroupController=io",
      "ConditionControlGroupController=pids",
      "ConditionControlGroupController=cpuset",
      "ConditionControlGroupController=blkio",
      "ConditionControlGroupController=cpuacct",
      "ConditionControlGroupController=devices",
      "ConditionControlGroupController=bpf-firewall",
      "ConditionControlGroupController=v1",
      "ConditionControlGroupController=v2",
      "ConditionControlGroupController=cpu memory",
      "AssertControlGroupController=|! cpu",
    )
  }

  @Test
  fun testControlGroupControllerListsMatchTheLongestControllerName() {
    // cg_mask_from_string splits on whitespace, and "cpu" is a prefix of both "cpuacct" and "cpuset".
    // The longest-first sort inside the terminal is what stops the first word of `cpuacct io` being
    // read as "cpu" and the rest being reported as garbage.
    assertAccepted(
      "ConditionControlGroupController=cpuacct io",
      "ConditionControlGroupController=cpuset cpu",
      "ConditionControlGroupController=cpu cpuacct cpuset io blkio memory devices pids",
      "ConditionControlGroupController=bpf-firewall bpf-devices",
    )
  }

  @Test
  fun testControlGroupControllerRejectsUnknownNames() {
    assertRejected("ConditionControlGroupController=invalid")
  }

  // ------------------------------------------------------------------ ConditionCPUFeature

  @Test
  fun testCpuFeatureAcceptsFeatureNames() {
    // Previously mapped onto the boolean grammar; /proc/cpuinfo flags have no closed list, so only
    // the shape is checked.
    assertAccepted(
      "ConditionCPUFeature=sse2",
      "ConditionCPUFeature=avx2",
      "ConditionCPUFeature=aes",
      "ConditionCPUFeature=x86-64.sse2",
      "AssertCPUFeature=|! sse2",
    )
  }

  @Test
  fun testCpuFeatureRejectsLists() {
    // condition_test_cpufeature never splits the parameter.
    assertRejected("ConditionCPUFeature=sse2 avx")
  }

  // ------------------------------------------------------------------ boolean conditions

  @Test
  fun testBooleanConditionsTakeEverySpellingWithTheMarkers() {
    // "Takes a boolean argument" -- systemd.unit(5), for both of these. The grammar is the shared
    // conditionString(BOOLEAN), i.e. [|] [!] <boolean>, which is what this validator has always
    // accepted; only the spelling of the marker prefix changed, to avoid an error range running past
    // the end of the value on inputs like `!!yes`.
    assertAccepted(
      "ConditionFirstBoot=yes",
      "ConditionFirstBoot=no",
      "ConditionFirstBoot=1",
      "ConditionFirstBoot=0",
      "ConditionFirstBoot=t",
      "ConditionFirstBoot=off",
      "AssertFirstBoot=|false",
      "AssertFirstBoot=!true",
      "AssertFirstBoot=|! true",
      "ConditionACPower=true",
      "AssertACPower=|yes",
    )
    assertRejected("ConditionFirstBoot=sometimes")
    assertRejected("ConditionACPower=maybe")
    assertRejected("ConditionFirstBoot=yes no")
  }
}
