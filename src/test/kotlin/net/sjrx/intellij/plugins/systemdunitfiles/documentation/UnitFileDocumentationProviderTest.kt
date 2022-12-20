package net.sjrx.intellij.plugins.systemdunitfiles.documentation

import com.intellij.lang.documentation.DocumentationProviderEx
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement
import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class UnitFileDocumentationProviderTest : AbstractUnitFileTest() {
  private val sut: DocumentationProviderEx = UnitFileDocumentationProvider()
  fun testGetUrlForUnknownKeyReturnsAnEmptyListOfUrls() {
    // Fixture Setup
    val file = """
           [Unit]
           BadKey=Hello Good Sir
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)


    // Exercise SUT
    val badKey = getAllKeysInFile(psiFile)[0]
    val urls = sut.getUrlFor(badKey, badKey)

    // Verification
    TestCase.assertNull(urls)
  }

  fun testGetUrlForUnknownKeySeparatorReturnsAnEmptyListOfUrls() {
    // Fixture Setup
    val file = """
           [Unit]
           BadKey=Hello Good Sir
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)


    // Exercise SUT
    val badKeySeparator = getAllSeparatorsInFile(psiFile)[0]
    val urls = sut.getUrlFor(badKeySeparator, badKeySeparator)

    // Verification
    TestCase.assertNull(urls)
  }

  fun testGetUrlForKnownKeyReturnsAValidUrl() {
    // Fixture Setup
    val file = """
           [Unit]
           Documentation=http://www.google.com
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)

    // Exercise SUT
    val documentationKey = getAllKeysInFile(psiFile)[0]
    val urls = sut.getUrlFor(documentationKey, documentationKey)

    // Verification
    TestCase.assertEquals(listOf("https://www.freedesktop.org/software/systemd/man/systemd.unit.html#Documentation="), urls)
  }

  fun testGetUrlForKnownKeySeparatorReturnsAValidUrl() {
    // Fixture Setup
    val file = """
           [Unit]
           Documentation=http://www.google.com
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)

    // Exercise SUT
    val documentationKeySeparator = getAllSeparatorsInFile(psiFile)[0]
    val urls = sut.getUrlFor(documentationKeySeparator, documentationKeySeparator)

    // Verification
    TestCase.assertEquals(listOf("https://www.freedesktop.org/software/systemd/man/systemd.unit.html#Documentation="), urls)
  }

  fun testGetUrlForDeprecatedOptionReturnsAValidUrl() {
    // Fixture Setup
    val file = """
           [Service]
           BlockIOAccounting=true
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)

    // Exercise SUT
    val documentationKeySeparator = getAllSeparatorsInFile(psiFile)[0]
    val urls = sut.getUrlFor(documentationKeySeparator, documentationKeySeparator)

    // Verification
    TestCase.assertEquals(listOf("https://www.freedesktop.org/software/systemd/man/systemd.resource-control.html#BlockIOAccounting="), urls)
  }

  fun testGetUrlForUnknownSectionReturnsAnEmptyListOfUrls() {
    // Fixture Setup
    val file = """
           [X-Unit]
           Documentation=http://www.google.com
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)

    // Exercise SUT
    val unknownSectionHeader = getAllSectionInFile(psiFile)[0]
    val urls = sut.getUrlFor(unknownSectionHeader, unknownSectionHeader)

    // Verification
    TestCase.assertNull(urls)
  }

  fun testGetUrlForKnownSectionReturnsAValidUrl() {
    // Fixture Setup
    val file = """
           [Unit]
           Documentation=http://www.google.com
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)

    // Exercise SUT
    val sectionHeader = getAllSectionInFile(psiFile)[0]
    val urls = sut.getUrlFor(sectionHeader, sectionHeader)

    // Verification
    TestCase.assertEquals(listOf("https://www.freedesktop.org/software/systemd/man/systemd.unit.html#%5BUnit%5D%20Section%20Options"), urls)
  }

  fun testGenerateDocForUnknownKeyReturnsNull() {
    // Fixture Setup
    val file = """
           [Unit]
           BadKey=Hello Good Sir
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)


    // Exercise SUT
    val badKey = getAllKeysInFile(psiFile)[0]
    val doc = sut.generateDoc(badKey, badKey)

    // Verification
    TestCase.assertNull(doc)
  }

  fun testGenerateDocForUnknownKeySeparatorReturnsNull() {
    // Fixture Setup
    val file = """
           [Unit]
           BadKey=Hello Good Sir
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)


    // Exercise SUT
    val badKeySeparator = getAllSeparatorsInFile(psiFile)[0]
    val doc = sut.generateDoc(badKeySeparator, badKeySeparator)

    // Verification
    TestCase.assertNull(doc)
  }

  fun testGenerateDocForKnownKeyReturnsSomeValidText() {
    // Fixture Setup
    val file = """
           [Unit]
           Documentation=http://www.google.com
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)

    // Exercise SUT
    val documentationKey = getAllKeysInFile(psiFile)[0]
    val doc = sut.generateDoc(documentationKey, documentationKey)

    // Verification
    TestCase.assertNotNull(doc)
    TestCase.assertTrue(doc!!.length > 0)
  }

  fun testGenerateDocForKnownKeySeparatorReturnsSomeValidText() {
    // Fixture Setup
    val file = """
           [Unit]
           Documentation=http://www.google.com
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)

    // Exercise SUT
    val documentationKeySeparator = getAllSeparatorsInFile(psiFile)[0]
    val doc = sut.generateDoc(documentationKeySeparator, documentationKeySeparator)

    // Verification
    TestCase.assertNotNull(doc)
    TestCase.assertTrue(doc!!.length > 0)
  }

  fun testGenerateDocForUnknownSectionReturnsNull() {
    // Fixture Setup
    val file = """
           [X-Unit]
           Documentation=http://www.google.com
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)

    // Exercise SUT
    val unknownSectionHeader = getAllSectionInFile(psiFile)[0]
    val doc = sut.generateDoc(unknownSectionHeader, unknownSectionHeader)

    // Verification
    TestCase.assertNull(doc)
  }

  fun testGenerateDocForKnownSectionReturnsSomeValidText() {
    // Fixture Setup
    val file = """
           [Unit]
           Documentation=http://www.google.com
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)

    // Exercise SUT
    val unknownSectionHeader = getAllSectionInFile(psiFile)[0]
    val doc = sut.generateDoc(unknownSectionHeader, unknownSectionHeader)

    // Verification
    TestCase.assertNotNull(doc)
    TestCase.assertTrue(doc!!.length > 1)
  }

  fun testGenerateDocForCompletedValueOfKnownKeyReturnsSomeValidText() {
    // Fixture Setup
    val file = """
           [Unit]
           Documentation=http://www.google.com
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)

    // Exercise SUT
    var documentationValue = getAllCompletedValuesInFile(psiFile)[0]
    documentationValue = sut.getCustomDocumentationElement(myFixture.editor, psiFile, documentationValue, 0)!!
    val doc = sut.generateDoc(documentationValue, documentationValue)

    // Verification
    TestCase.assertNotNull(doc)
    TestCase.assertTrue(doc!!.length > 0)
  }

  fun testGenerateDocForCompletedValueOfUnknownSectionReturnsNull() {
    // Fixture Setup
    val file = """
           [X-Unit]
           Documentation=http://www.google.com
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)

    // Exercise SUT
    var documentationValue = getAllCompletedValuesInFile(psiFile)[0]
    documentationValue = sut.getCustomDocumentationElement(myFixture.editor, psiFile, documentationValue, 0)!!
    val doc = sut.generateDoc(documentationValue, documentationValue)

    // Verification
    TestCase.assertNull(doc)
  }

  fun testGenerateDocForCompletedValueOfUnknownKeyReturnsNull() {
    // Fixture Setup
    val file = """
           [Unit]
           HelpPage=http://www.google.com
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)

    // Exercise SUT
    var documentationValue = getAllCompletedValuesInFile(psiFile)[0]
    documentationValue = sut.getCustomDocumentationElement(myFixture.editor, psiFile, documentationValue, 0)!!
    val doc = sut.generateDoc(documentationValue, documentationValue)

    // Verification
    TestCase.assertNull(doc)
  }

  fun testGenerateDocForContinuingValueOfKnownKeyReturnsSomeValidText() {
    // Fixture Setup
    val file = """
           [Unit]
           Documentation=http://www.google.com\
           hello sir
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)

    // Exercise SUT
    var documentationValue = getAllContinuingValuesInFile(psiFile)[0]
    documentationValue = sut.getCustomDocumentationElement(myFixture.editor, psiFile, documentationValue, 0)!!
    val doc = sut.generateDoc(documentationValue, documentationValue)

    // Verification
    TestCase.assertNotNull(doc)
    TestCase.assertTrue(doc!!.length > 0)
  }

  fun testGenerateDocForContinuingValueOfUnknownSectionReturnsNull() {
    // Fixture Setup
    val file = """
           [X-Unit]
           Documentation=http://www.google.com\
           hello sir
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)

    // Exercise SUT
    var documentationValue = getAllContinuingValuesInFile(psiFile)[0]
    documentationValue = sut.getCustomDocumentationElement(myFixture.editor, psiFile, documentationValue, 0)!!
    val doc = sut.generateDoc(documentationValue, documentationValue)

    // Verification
    TestCase.assertNull(doc)
  }

  fun testGenerateDocForContinuingValueOfUnknownKeyReturnsNull() {
    // Fixture Setup
    val file = """
           [Unit]
           HelpPage=http://www.google.com\
           hello sir
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)

    // Exercise SUT
    var documentationValue = getAllContinuingValuesInFile(psiFile)[0]
    documentationValue = sut.getCustomDocumentationElement(myFixture.editor, psiFile, documentationValue, 0)!!
    val doc = sut.generateDoc(documentationValue, documentationValue)

    // Verification
    TestCase.assertNull(doc)
  }

  fun testGenerateDocForContinuingValueAfterCommentsOfKnownKeyReturnsSomeValidText() {
    // Fixture Setup
    val file = """
           [Unit]
           Documentation=http://www.google.com\
           ;Good bye
           #See ya later!
           hello sir
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)

    // Exercise SUT
    var documentationValue = getAllContinuingValuesInFile(psiFile)[0]
    documentationValue = sut.getCustomDocumentationElement(myFixture.editor, psiFile, documentationValue, 0)!!
    val doc = sut.generateDoc(documentationValue, documentationValue)

    // Verification
    TestCase.assertNotNull(doc)
    TestCase.assertTrue(doc!!.length > 0)
  }

  fun testGenerateDocForContinuingValueAfterCommentsOfUnknownSectionReturnsNull() {
    // Fixture Setup
    val file = """
           [X-Unit]
           Documentation=http://www.google.com\
           ;Good bye
           #See ya later!
           hello sir
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)

    // Exercise SUT
    var documentationValue = getAllContinuingValuesInFile(psiFile)[0]
    documentationValue = sut.getCustomDocumentationElement(myFixture.editor, psiFile, documentationValue, 0)!!
    val doc = sut.generateDoc(documentationValue, documentationValue)

    // Verification
    TestCase.assertNull(doc)
  }

  fun testGenerateDocForContinuingValueAfterCommentsOfUnknownKeyReturnsNull() {
    // Fixture Setup
    val file = """
           [Unit]
           HelpPage=http://www.google.com\
           ;Good bye
           #See ya later!
           hello sir
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)

    // Exercise SUT
    var documentationValue = getAllContinuingValuesInFile(psiFile)[0]
    documentationValue = sut.getCustomDocumentationElement(myFixture.editor, psiFile, documentationValue, 0)!!
    val doc = sut.generateDoc(documentationValue, documentationValue)

    // Verification
    TestCase.assertNull(doc)
  }

  fun testGenerateDocMovedSectionReturnsSomeText() {
    // Fixture Setup
    val file = """
           [Unit]
           PropagateReloadTo=cpu.mount
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)

    // Exercise SUT
    val renamedSection = getAllKeysInFile(psiFile)[0]
    val doc = sut.generateDoc(renamedSection, renamedSection)

    // Verification
    TestCase.assertNotNull(doc)
    TestCase.assertEquals(
      "Expected the generated documentation to match", "<div class='definition'><pre>PropagateReloadTo</pre>"
        + "</div><div class='content'><p>The key <var>PropagateReloadTo</var> in"
        + " section <b>Unit</b> has been renamed to <var>PropagateReloadsTo</var>"
        + "<p>NOTE: The semantics of the new value may not"
        + " match the existing value.<p>"
        + "<a href='https://github.com/systemd/systemd/commit/7f2cddae09'>More"
        + " information is available here</a></div>", doc
    )
  }

  fun testGenerateDocForUnsupportedSectionReturnsSomeValidText() {
    // Fixture Setup
    val file = """
           [Service]
           PermissionsStartOnly=true
           """.trimIndent()
    val psiFile = setupFileInEditor("file.service", file)

    // Exercise SUT
    val unsupportedSection = getAllKeysInFile(psiFile)[0]
    val doc = sut.generateDoc(unsupportedSection, unsupportedSection)

    // Verification
    TestCase.assertNotNull(doc)
    TestCase.assertEquals(
      "Expected the generated documentation to match", "<div class='definition'><pre>PermissionsStartOnly"
        + "</pre></div><div class='content'><p><var>PermissionsStartOnly</var> in"
        + " section <b>Service</b> is not officially supported.<p>"
        + "<a href='https://github.com/systemd/systemd/blob/v241/NEWS#L561'>"
        + "More information is available here</a></div>", doc
    )
  }

}
