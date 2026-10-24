package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/**
 * Pins the #568 decisions for `Condition…=` / `Assert…=` values that systemd's config parser stores
 * without complaint but that the runtime check in src/shared/condition.c can never satisfy.
 *
 * config_parse_unit_condition_string strips at most ONE `|` and then at most ONE `!`, each followed by
 * optional whitespace, and stores everything after that verbatim as the parameter:
 * https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L3167-L3173
 *
 * So `!! x`, `!| x`, `|| x`, `! | x` and `| | x` do not stack markers; the second marker becomes the
 * first character of the parameter. None of the condition_test_* checks below accept a parameter that
 * starts with `!` or `|`: they return false (never matches) or an error, which
 * condition_test_list_impl treats as a failed/undeterminable condition:
 * https://github.com/systemd/systemd/blob/0f0682b773/src/shared/condition.c#L1465-L1529
 *
 * condition.c, not the parser, is the oracle for conditions, so these stay flagged.
 */
class Fix568ConditionMarkerVerdictsTest : AbstractUnitFileTest() {

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

  @Test
  fun testSingleMarkersWithOptionalWhitespaceAreAccepted() {
    assertAccepted(
      "AssertACPower=! true",
      "AssertACPower=| true",
      "AssertACPower=| ! true",
      "AssertArchitecture=|!alpha",
      "AssertCapability=| ! CAP_CHOWN",
      "AssertSecurity=!audit",
      "AssertVirtualization=| ! vm",
      "AssertControlGroupController=| ! cpu",
    )
  }

  @Test
  fun testDoubledMarkersOnBooleanConditionsAreRejected() {
    // condition_test_ac_power: parse_boolean("! true") / ("| true") fails -> -EINVAL.
    // https://github.com/systemd/systemd/blob/0f0682b773/src/shared/condition.c#L850-L862
    assertRejected("AssertACPower=!! true")
    assertRejected("AssertACPower=! | true")
    assertRejected("AssertACPower=| | true")
  }

  @Test
  fun testDoubledMarkersOnNameConditionsAreRejected() {
    // condition_test_architecture_parameter: architecture_from_string("| alpha") fails -> false.
    // https://github.com/systemd/systemd/blob/0f0682b773/src/shared/condition.c#L561-L579
    assertRejected("AssertArchitecture=!! alpha")
    assertRejected("AssertArchitecture=!| alpha")
    assertRejected("AssertArchitecture=|| alpha")
    // condition_test_capability: capability_from_name("! CAP_CHOWN") fails -> -EINVAL.
    // https://github.com/systemd/systemd/blob/0f0682b773/src/shared/condition.c#L905-L923
    assertRejected("AssertCapability=!! CAP_CHOWN")
    assertRejected("AssertCapability=!| CAP_CHOWN")
    assertRejected("AssertCapability=|| CAP_CHOWN")
    // condition_test_security: only exact names match, anything else -> false.
    // https://github.com/systemd/systemd/blob/0f0682b773/src/shared/condition.c#L874-L903
    assertRejected("AssertSecurity=!! audit")
    assertRejected("AssertSecurity=!| audit")
    assertRejected("AssertSecurity=|| audit")
    // condition_test_virtualization: "| true" is no boolean, category or id -> false.
    // https://github.com/systemd/systemd/blob/0f0682b773/src/shared/condition.c#L530-L559
    assertRejected("AssertVirtualization=!! true")
    assertRejected("AssertVirtualization=! | true")
    assertRejected("AssertVirtualization=| | true")
    assertRejected("AssertVirtualization=true false")
  }

  @Test
  fun testDoubledMarkersOnControlGroupControllerAreRejected() {
    // The one subtle case: cg_mask_from_string splits "! cpu" into words and silently skips the
    // unknown "!", so `!! cpu` behaves exactly like `!cpu` rather than as a double negation.
    // https://github.com/systemd/systemd/blob/0f0682b773/src/basic/cgroup-util.c#L1661-L1686
    // The stray marker is a no-op word, like any other unknown controller name, which this validator
    // already flags (an all-unknown list makes the check unconditionally true, condition.c#L500-L506):
    // https://github.com/systemd/systemd/blob/0f0682b773/src/shared/condition.c#L482-L509
    assertRejected("AssertControlGroupController=!! cpu")
    assertRejected("AssertControlGroupController=!| cpu")
    assertRejected("AssertControlGroupController=|| cpu")
    assertRejected("AssertControlGroupController=invalid")
  }
}
