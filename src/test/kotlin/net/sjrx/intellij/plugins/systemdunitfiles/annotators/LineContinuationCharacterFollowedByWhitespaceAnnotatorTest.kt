package net.sjrx.intellij.plugins.systemdunitfiles.annotators

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class LineContinuationCharacterFollowedByWhitespaceAnnotatorTest : AbstractUnitFileTest() {
  fun testThatExcessWhiteSpaceAfterLineContinuationCharacterIsDetectedAsWarningInSimpleCase() {
    // Fixture Setup
    val file = ("[Service]\n"
      + "ExecStart=/bin/bash \\ \n"
      + "X-Garbage=")
    setupFileInEditor("file.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(LineContinuationCharacterFollowedByWhitespaceAnnotator.WARNING_MESSAGE, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    TestCase.assertEquals(" ", info.text)
  }

  fun testThatExcessWhiteSpaceAfterLineContinuationCharacterIsDetectedAsWarningInLongerSimpleCase() {
    // Fixture Setup
    val file = ("[Service]\n"
      + "ExecStart=/bin/bash \\       \n"
      + "X-Garbage=")
    setupFileInEditor("file.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(LineContinuationCharacterFollowedByWhitespaceAnnotator.WARNING_MESSAGE, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    TestCase.assertEquals("       ", info.text)
  }

  fun testThatExcessWhiteSpaceAfterLineContinuationCharacterIsDetectedOnASuccessfulLineContinuation() {

    // Fixture Setup
    val file = ("[Service]\n"
      + "ExecStart=/bin/bash \\\n"
      + "X-Garbage=oeutnhouentuoeh\\ \t \n"
      + "X-Garbage-Two=")
    setupFileInEditor("file.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(LineContinuationCharacterFollowedByWhitespaceAnnotator.WARNING_MESSAGE, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    TestCase.assertEquals(" \t ", info.text)
  }

  fun testThatExcessWhiteSpaceAfterLineContinuationCharacterIsDetectedOnASuccessfulLineContinuationWithComment() {
    // Fixture Setup
    val file = ("[Service]\n"
      + "ExecStart=/bin/bash \\\n"
      + ";A nice comment\n"
      + "X-Garbage=\\\t\t\t\n"
      + "X-Garbage-Two=")
    setupFileInEditor("file.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(LineContinuationCharacterFollowedByWhitespaceAnnotator.WARNING_MESSAGE, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    TestCase.assertEquals("\t\t\t", info.text)
  }

  fun testThatNoExcessWhitespaceAfterLineContinuationCharacterResultsInNoWarnings() {
    // Fixture Setup
    val file = """
           [Service]
           ExecStart=/bin/bash \
           ;A nice comment
           X-Garbage=\
           X-Garbage-Two=
           """.trimIndent()
    setupFileInEditor("file.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(0, highlights)
  }

  fun testThatNoLineContinuationCharacterResultsInNoWarnings() {
    // Fixture Setup
    val file = ("[Service]\n"
      + "ExecStart=/bin/bash \\\n"
      + ";A nice comment\n"
      + "X-Garbage=\t    \\\n"
      + "X-Garbage-Two=")
    setupFileInEditor("file.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }
}
