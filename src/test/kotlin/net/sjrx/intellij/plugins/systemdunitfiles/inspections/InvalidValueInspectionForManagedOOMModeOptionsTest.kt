package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValueInspectionForManagedOOMModeOptionsTest : AbstractUnitFileTest() {

  fun testNoWarningWhenAutoSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Swap]
           ManagedOOMSwap=auto
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.swap", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenKillSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Swap]
           ManagedOOMSwap=kill
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.swap", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Swap]
           ManagedOOMSwap=foo
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.swap", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }
}
