package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValueInspectionForImagePolicy : AbstractUnitFileTest() {

  fun testNoWarningWhenNumberSpecifiedWithoutUnit() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           RootImagePolicy=root=unprotected
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)

  }

  // image_policy_from_string() runs in graceful mode for unit files, so an unknown partition designator or policy
  // flag (and the empty flag after a trailing '+') is skipped rather than refused. These values used to be asserted
  // invalid; systemd's parser accepts every one of them (#568).
  // https://github.com/systemd/systemd/blob/0f0682b773/src/shared/image-policy.c#L341-L465

  fun testNoWarningWhenUnknownPartitionTypeIsSpecified() {
    assertNoHighlights("opt=unprotected")
  }

  fun testNoWarningWhenUnknownPolicyFlagIsSpecified() {
    assertNoHighlights("root=unsigned")
  }

  fun testNoWarningWhenUnknownPolicyFlagIsSpecifiedInSecondaryPolicy() {
    assertNoHighlights("home=encrypted+used")
  }

  fun testNoWarningWhenPolicyFlagsEndWithPlus() {
    assertNoHighlights("home=encrypted+absent+")
  }

  fun testNoWarningWhenUnknownPolicyFlagIsSetInSecondPolicy() {
    assertNoHighlights("home=encrypted:root=encrypted+used")
  }

  fun testWarningWhenEqualsIsMissing() {
    // "Missing '=' in policy expression" is a hard error even in graceful mode.
    // language="unit file (systemd)"
    val file = """
           [Service]
           RootImagePolicy=root
           """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(1, highlights)
    assertStringContains("RootImagePolicy's value does not match the expected format", highlights[0]!!.description)
  }

  private fun assertNoHighlights(value: String) {
    setupFileInEditor("file.service", "[Service]\nRootImagePolicy=$value\n")
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

}
