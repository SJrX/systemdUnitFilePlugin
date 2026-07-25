package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

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
  fun testPathConditionsRejectLists() {
    // The whole value is one path — it is never split on whitespace.
    assertRejected("ConditionNeedsUpdate=/etc /var")
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
  fun testBooleanConditionsStillWork() {
    assertAccepted(
      "ConditionFirstBoot=yes",
      "ConditionACPower=true",
      "AssertFirstBoot=|false",
    )
    assertRejected("ConditionFirstBoot=sometimes")
  }
}
