package net.sjrx.intellij.plugins.systemdunitfiles.annotators

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.openapi.util.TextRange
import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class PidFileOptionWarningTest : AbstractUnitFileTest() {

  fun testPidFileAnnotated() {

    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           Type=forking
           PIDFile=/run/foo.pid

           """.trimIndent()

    setupFileInEditor("file.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(PidFileOptionWarning.ANNOTATION_ERROR_MSG, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WEAK_WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    val annotatedText = myFixture.getDocument(myFixture.file).getText(TextRange.create(info.startOffset, info.endOffset))
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("PIDFile", annotatedText)
  }

  fun testPidFileInXSectionNotAnnotated() {

    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [X-TestService]
           PIDFile=/run/foo.pid

           """.trimIndent()

    setupFileInEditor("file.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

}
