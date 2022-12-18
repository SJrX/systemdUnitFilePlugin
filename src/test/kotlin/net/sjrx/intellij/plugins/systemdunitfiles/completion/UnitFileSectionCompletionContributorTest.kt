package net.sjrx.intellij.plugins.systemdunitfiles.completion

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class UnitFileSectionCompletionContributorTest : AbstractUnitFileTest() {

  fun testCompletionOfNewSectionHeaderReturnsExpectedValuesInAutomount() {
    // Fixture Setup
    val file = """
           [Install]
           Whatevs=Foo
           
           [$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.automount", file)
    val completions = basicCompletionResultStrings
    assertContainsElements(completions, "Install", "Unit", "Automount")
  }

  fun testCompletionOfNewSectionHeaderReturnsExpectedValuesInDevice() {
    // Fixture Setup
    val file = """
           [Install]
           Whatevs=Foo
           
           [$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.device", file)
    val completions = basicCompletionResultStrings
    assertContainsElements(completions, "Install", "Unit")
  }

  fun testCompletionOfNewSectionHeaderReturnsExpectedValuesInTarget() {
    // Fixture Setup
    val file = """
           [Install]
           Whatevs=Foo
           
           [$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.target", file)
    val completions = basicCompletionResultStrings
    assertContainsElements(completions, "Install", "Unit")
  }

  fun testCompletionOfNewSectionHeaderReturnsExpectedValuesInMount() {
    // Fixture Setup
    val file = """
           [Install]
           Whatevs=Foo
           
           [$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.mount", file)
    val completions = basicCompletionResultStrings
    assertContainsElements(completions, "Install", "Unit", "Mount")
  }

  fun testCompletionOfNewSectionHeaderReturnsExpectedValuesInPath() {
    // Fixture Setup
    val file = """
           [Install]
           Whatevs=Foo
           
           [$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.path", file)
    val completions = basicCompletionResultStrings
    assertContainsElements(completions, "Install", "Unit", "Path")
  }

  fun testCompletionOfNewSectionHeaderReturnsExpectedValuesInService() {
    // Fixture Setup
    val file = """
           [Install]
           Whatevs=Foo
           
           [$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.service", file)
    val completions = basicCompletionResultStrings
    assertContainsElements(completions, "Install", "Unit", "Service")
  }

  fun testCompletionOfNewSectionHeaderReturnsExpectedValuesInSocket() {
    // Fixture Setup
    val file = """
           [Install]
           Whatevs=Foo
           
           [$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.socket", file)
    val completions = basicCompletionResultStrings
    assertContainsElements(completions, "Install", "Unit", "Socket")
  }

  fun testCompletionOfNewSectionHeaderReturnsExpectedValuesInSwap() {
    // Fixture Setup
    val file = """
           [Install]
           Whatevs=Foo
           
           [$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.swap", file)
    val completions = basicCompletionResultStrings
    assertContainsElements(completions, "Install", "Unit", "Swap")
  }

  fun testCompletionOfNewSectionHeaderReturnsExpectedValuesInTimer() {
    // Fixture Setup
    val file = """
           [Install]
           Whatevs=Foo
           
           [$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.timer", file)
    val completions = basicCompletionResultStrings
    assertContainsElements(completions, "Install", "Unit", "Timer")
  }

  fun testCompletionOfNewSectionInUnknownFileTypeIsEmpty() {
    // Fixture Setup
    val file = """
           [Tester]
           Whatevs=$COMPLETION_POSITION

           """.trimIndent()
    myFixture.configureByText("file.mystery", file)

    // Execute SUT
    val completions = basicCompletionResultStrings

    // Verification
    assertSize(0, completions)
  }
}
