package net.sjrx.intellij.plugins.systemdunitfiles.annotators

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.openapi.util.TextRange
import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class PropertyIsNotInSectionAnnotatorTest : AbstractUnitFileTest() {
  fun testAnnotated() {
    // Fixture Setup
    val file = """
           Key=Value
           [Service]
           Second=Value
           
           """.trimIndent()
    setupFileInEditor("file.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(PropertyIsNotInSectionAnnotator.ANNOTATION_ERROR_MSG, info!!.description)
    TestCase.assertEquals(HighlightInfoType.ERROR, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    val annotatedText = myFixture.getDocument(myFixture.file).getText(TextRange.create(info.startOffset, info.endOffset))
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("Key=Value", annotatedText)
  }

  fun testNotAnnotated() {
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
