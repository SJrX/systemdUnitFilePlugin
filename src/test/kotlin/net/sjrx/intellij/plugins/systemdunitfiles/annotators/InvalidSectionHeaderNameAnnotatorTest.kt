package net.sjrx.intellij.plugins.systemdunitfiles.annotators

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidSectionHeaderNameAnnotatorTest : AbstractUnitFileTest() {
  fun testThatInvalidSectionNamesAreAnnotated() {
    // Fixture Setup
    val file = ("[Serv\tice]\n"
      + "Requires=Hello Good Sir")
    setupFileInEditor("file.service", file)


    // Exercise SUT
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(InvalidSectionHeaderNameAnnotator.ANNOTATION_ERROR_MSG, info!!.description)
    TestCase.assertEquals(HighlightInfoType.ERROR, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("[Serv\tice]", highlightElement!!.text)
  }

  fun testThatInvalidSectionNameWithLeftBraceAreAnnotated() {
    // Fixture Setup
    val file = """
           [Serv[ice]
           Requires=Hello Good Sir
           """.trimIndent()
    setupFileInEditor("file.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(InvalidSectionHeaderNameAnnotator.ANNOTATION_ERROR_MSG, info!!.description)
    TestCase.assertEquals(HighlightInfoType.ERROR, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("[Serv[ice]", highlightElement!!.text)
  }

  fun testThatValidSectionNamesAreNotAnnotated() {
    // Fixture Setup
    val file = """
           [Service]
           Requires=Hello Good Sir
           """.trimIndent()
    setupFileInEditor("file.service", file)


    // Exercise SUT
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(0, highlights)
  }

  fun testUnterminatedSectionName() {
    // Fixture Setup
    val file = """
           [Service
           Foo=Bar
           """.trimIndent()
    setupFileInEditor("file.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(InvalidSectionHeaderNameAnnotator.ANNOTATION_ERROR_MSG, info!!.description)
    TestCase.assertEquals(HighlightInfoType.ERROR, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("[Service", highlightElement!!.text)
  }
}
