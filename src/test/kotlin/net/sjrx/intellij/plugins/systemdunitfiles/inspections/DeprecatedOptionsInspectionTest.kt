package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class DeprecatedOptionsInspectionTest : AbstractUnitFileTest() {
  fun testNonDeprecatedOptionDoesNotThrowError() {
    val file = """
           [Service]
           ExecStart=/bin/bash
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(DeprecatedOptionsInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(0, highlights)
  }

  fun testUnknownOptionDoesNotThrowError() {
    val file = """
           [Service]
           SomeOption=/bin/bash
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(DeprecatedOptionsInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(0, highlights)
  }

  fun testSingleExampleThrowsWarning() {
    val file = """
           [Service]
           InaccessibleDirectories=/boo
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(DeprecatedOptionsInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals("'InaccessibleDirectories' in section 'Service' has been renamed to 'InaccessiblePath'", info!!.description)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("InaccessibleDirectories", highlightElement!!.text)
  }

}
