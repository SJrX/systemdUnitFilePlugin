package net.sjrx.intellij.plugins.systemdunitfiles.completion

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class UnitFileKeyCompletionContributorTest : AbstractUnitFileTest() {
  fun testCompletionInInstallSectionReturnsExpectedValues() {
    // Fixture Setup
    val file = """
           [Install]
           Al$COMPLETION_POSITION
           DefaultInstance=thueo
           """.trimIndent()
    myFixture.configureByText("file.service", file)

    // Exercise SUT
    val completions = basicCompletionResultStrings

    // Verification
    assertContainsElements(completions, "Alias", "Also")
  }

  fun testCompletionInInstallSectionReturnsExpectedValuesWhenAtEndOfFile() {
    // Fixture Setup
    val file = """
           [Install]
           Al$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.service", file)

    // Exercise SUT
    val completions = basicCompletionResultStrings

    // Verification
    assertContainsElements(completions, "Alias", "Also")
    assertSize(2, completions)
  }

  fun testCompletionOfImpossibleToMatchKeyReturnsEmpty() {
    // Fixture Setup
    val file = """
           [Install]
           ZzzZZZZ$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.service", file)

    // Exercise SUT
    val completions = basicCompletionResultStrings

    // Verification
    assertEmpty(completions)
  }

  fun testCompletionInUnknownSectionReturnsEmpty() {
    // Fixture Setup
    val file = """
           [X-Unknown]
           Al$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.service", file)

    // Exercise SUT
    val completions = basicCompletionResultStrings

    // Verification
    assertEmpty(completions)
  }

  fun testCompletionInPathSectionReturnsExpectedValuesWhenAtEndOfFile() {
    // Fixture Setup
    val file = """
           [Path]
           Pat$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.service", file)

    // Exercise SUT
    val completions = basicCompletionResultStrings

    // Verification
    assertContainsElements(completions, "PathExists", "PathExistsGlob", "PathChanged", "PathModified")
    assertSize(4, completions)
  }

  fun testCompletionAfterKeyWithEmptyValue() {
    // Fixture Setup
    val file = """
           [Swap]
           M
           
           [Path]
           MakeDirectory=
           M$COMPLETION_POSITION
           
           
           """.trimIndent()
    myFixture.configureByText("file.service", file)

    // Exercise SUT
    val completions = basicCompletionResultStrings

    // Verification
    assertContainsElements(completions, "DirectoryMode", "PathModified", "DirectoryNotEmpty")
    assertDoesntContain(completions, "MakeDirectory")
  }

  fun testCompletionBeforeKeyWithEmptyValue() {
    // Fixture Setup
    val file = """
           [Swap]
           M
           
           [Path]
           M$COMPLETION_POSITION
           MakeDirectory=
           
           
           """.trimIndent()
    myFixture.configureByText("file.service", file)

    // Exercise SUT
    val completions = basicCompletionResultStrings

    // Verification
    assertContainsElements(completions, "DirectoryMode", "PathModified", "DirectoryNotEmpty")
    assertDoesntContain(completions, "MakeDirectory")
  }

  fun testCompletionAfterComment() {
    // Fixture Setup
    val file = """
           [Swap]
           M
           
           [Path]
           ;comment
           $COMPLETION_POSITION
           
           
           """.trimIndent()
    myFixture.configureByText("file.service", file)

    // Exercise SUT
    val completions = basicCompletionResultStrings

    // Verification
    assertContainsElements(completions, "DirectoryMode", "DirectoryNotEmpty", "MakeDirectory", "PathModified")
  }
}
