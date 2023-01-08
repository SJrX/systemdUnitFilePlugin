package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValueInspectionForEmergencyActionOptionValueTest : AbstractUnitFileTest() {

  fun testNoWarningWhenNoneSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           JobTimeoutAction=none
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenRebootSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           JobTimeoutAction=reboot
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenRebootForceSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           JobTimeoutAction=reboot-force
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenRebootImmediateSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           JobTimeoutAction=reboot-immediate
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenPoweroffSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           JobTimeoutAction=poweroff
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenPoweroffForceSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           JobTimeoutAction=poweroff-force
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenPoweroffImmediateSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           JobTimeoutAction=poweroff-immediate
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenExitSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           JobTimeoutAction=exit
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testNoWarningWhenExitForceSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           JobTimeoutAction=exit-force
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenUnknownValueSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Unit]
           JobTimeoutAction=foo
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }

}
