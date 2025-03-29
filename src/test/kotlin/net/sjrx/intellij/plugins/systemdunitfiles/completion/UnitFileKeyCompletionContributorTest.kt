package net.sjrx.intellij.plugins.systemdunitfiles.completion

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class UnitFileKeyCompletionContributorTest : AbstractUnitFileTest() {
  fun testCompletionInInstallSectionReturnsExpectedValues() {
    // Fixture Setup
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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

  fun testCompletionForNSpawnFileInFilesSectionReturnsExpectedValues() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Files]
           Bi$COMPLETION_POSITION
           Inaccessible=True
           """.trimIndent()
    myFixture.configureByText("file.nspawn", file)

    // Exercise SUT
    val completions = basicCompletionResultStrings

    // Verification
    assertContainsElements(completions, "Bind", "BindReadOnly", "BindUser")
  }

  fun testCompletionForLinkFileInMatchSectionReturnsExpectedValues() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Match]
           Ke$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.link", file)

    // Exercise SUT
    val completions = basicCompletionResultStrings

    // Verification
    assertContainsElements(completions, "KernelCommandLine", "KernelVersion")
  }

  fun testCompletionForNetworkFileInMatchSectionReturnsExpectedValues() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Match]
           Ke$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.network", file)

    // Exercise SUT
    val completions = basicCompletionResultStrings

    // Verification
    assertContainsElements(completions, "KernelCommandLine", "KernelVersion")
  }

  fun testCompletionForNetDevFileInMatchSectionReturnsExpectedValues() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Match]
           Ke$COMPLETION_POSITION
           """.trimIndent()
    myFixture.configureByText("file.netdev", file)

    // Exercise SUT
    val completions = basicCompletionResultStrings

    // Verification
    assertContainsElements(completions, "KernelCommandLine", "KernelVersion")
  }

}
