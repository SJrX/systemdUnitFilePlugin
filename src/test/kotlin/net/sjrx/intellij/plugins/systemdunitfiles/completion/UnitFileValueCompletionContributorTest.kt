package net.sjrx.intellij.plugins.systemdunitfiles.completion

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class UnitFileValueCompletionContributorTest : AbstractUnitFileTest() {
  fun testCompletionOfUnknownKeyInKnownSectionIsEmpty() {
    // Fixture Setup
    val file = """
           [Install]
           Whatevs=$COMPLETION_POSITION
           
           """.trimIndent()
    myFixture.configureByText("file.service", file)
    val completions = basicCompletionResultStrings
    assertSize(0, completions)
  }

  fun testCompletionOfUnknownKeyInUnKnownSectionIsEmpty() {
    // Fixture Setup
    val file = """
           [Tester]
           Whatevs=$COMPLETION_POSITION
           
           """.trimIndent()
    myFixture.configureByText("file.service", file)

    // Execute SUT
    val completions = basicCompletionResultStrings

    // Verification
    assertSize(0, completions)
  }

  fun testCompletionOfBooleanOptionReturnsValues() {
    // Fixture Setup
    val file = """
           [Service]
           TTYReset=$COMPLETION_POSITION
           
           """.trimIndent()
    myFixture.configureByText("file.service", file)

    // Execute SUT
    val completions = basicCompletionResultStrings

    // Verification
    assertContainsElements(completions, "on", "off", "true", "false", "yes", "no")
  }

  fun testCompletionOfKillModeOptionReturnsValues() {
    // Fixture Setup
    val file = """
           [Service]
           KillMode=o$COMPLETION_POSITION
           
           """.trimIndent()
    myFixture.configureByText("file.service", file)

    // Execute SUT
    val completions = basicCompletionResultStrings

    // Verification
    assertContainsElements(completions, "control-group", "process", "none")
  }

  fun testCompletionOfRestartOptionReturnsValues() {
    // Fixture Setup
    val file = """
           [Service]
           Restart=or$COMPLETION_POSITION
           
           """.trimIndent()
    myFixture.configureByText("file.service", file)

    // Execute SUT
    val completions = basicCompletionResultStrings

    // Verification
    assertContainsElements(completions, "on-abnormal", "on-abort")
  }

  fun testCompletionOfServiceTypeOptionReturnsValues() {
    // Fixture Setup
    val file = """
           [Service]
           Type=o$COMPLETION_POSITION
           
           """.trimIndent()
    myFixture.configureByText("file.service", file)

    // Execute SUT
    val completions = basicCompletionResultStrings

    // Verification
    assertContainsElements(completions, "forking", "oneshot", "notify")
  }

  fun testCompletionOfUnitDependencyIncludesUnitsInFilename() {
    // Fixture Setup
    val file = """
           [Unit]
           After=file.servi$COMPLETION_POSITION
           
           """.trimIndent()
    myFixture.configureByText("file.service", file)

    // Execute SUT
    val completions = basicCompletionResultStrings

    // Verification
    assertContainsElements(completions, "file.service")
  }

  fun testCompletionOfUnitDependencyIncludesUnitsInUbuntu() {
    // Fixture Setup
    val file = """
           [Unit]
           After=NetworkM$COMPLETION_POSITION
           
           """.trimIndent()
    myFixture.configureByText("file.service", file)

    // Execute SUT
    val completions = basicCompletionResultStrings

    // Verification
    assertContainsElements(completions, "NetworkManager.service")
  }
}
