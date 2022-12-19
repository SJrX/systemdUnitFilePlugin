package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class ShellSyntaxInExecDirectiveInspectionTest : AbstractUnitFileTest() {
  fun testValidSimpleExecCallDoesNotThrowError() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStart=docker-compose
           """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(ShellSyntaxInExecDirectiveInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testValidMultipleArgumentExecCallDoesNotGenerateError() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStop=docker-compose -f docker-compose.yml down
           """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(ShellSyntaxInExecDirectiveInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testRandomOptionDoesNotGenerateError() {
    // Fixture Setup
    val file = """
           [Service]
           OnSuccessMode=docker-compose -f docker-compose.yml down > log.txt
           """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(ShellSyntaxInExecDirectiveInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testSimpleExecCallWithRedirectDoesThrowError() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStart=docker-compose>file.txt
           """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(ShellSyntaxInExecDirectiveInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }

  fun testSimpleExecCallWithTwoSpecialCharactersGeneratesTwoErrors() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStart=docker-compose < hello.txt > file.txt
           """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(ShellSyntaxInExecDirectiveInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(2, highlights)
  }

  fun testSimpleExecCallWithAppendSpecialCharactersOnlyHasTwoErrors() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStart=docker-compose << hello.txt >> file.txt
           """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(ShellSyntaxInExecDirectiveInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(2, highlights)
  }

  fun testSimpleExecCallWithPipeHasError() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStart=docker-compose | grep foo
           """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(ShellSyntaxInExecDirectiveInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }

  fun testSimpleExecCallWithBackgroundHasError() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStart=docker-compose up --build&
           """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(ShellSyntaxInExecDirectiveInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
  }

  fun testWrappedCallWithDoubleQuotesAllMetaCharactersHasNoErrors() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStart=/bin/sh -c "a < b << c > d >> e | f&"
           
           """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(ShellSyntaxInExecDirectiveInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWrappedCallWithSingleQuotesAllMetaCharactersHasNoErrors() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStart=/bin/sh -c 'a < b << c > d >> e | f&'
           
           """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(ShellSyntaxInExecDirectiveInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }
}
