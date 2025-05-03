package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class DeprecatedOptionsInspectionTest : AbstractUnitFileTest() {
  fun testNonDeprecatedOptionDoesNotThrowError() {
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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


  fun testSingleExampleInNSpawnFileThrowsWarning() {

    // language="unit file (systemd)"
    val file = """
           [Files]
           PrivateUsersChown=true
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.nspawn", file)
    enableInspection(DeprecatedOptionsInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals("'PrivateUsersChown' in section 'Files' has been renamed to 'PrivateUsersOwnership'", info!!.description)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("PrivateUsersChown", highlightElement!!.text)
  }

  fun testSingleExampleInNetdevFileThrowsWarning() {

    // language="unit file (systemd)"
    val file = """
           [VRF]
           TableId=2145
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.netdev", file)
    enableInspection(DeprecatedOptionsInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals("'TableId' in section 'VRF' has been renamed to 'Table'", info!!.description)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("TableId", highlightElement!!.text)
  }


  fun testSingleExampleInNetworkFileThrowsWarning() {

    // language="unit file (systemd)"
    val file = """
           [Address]
           PrefixRoute=10.0.0.1
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.network", file)
    enableInspection(DeprecatedOptionsInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals("'PrefixRoute' in section 'Address' has been renamed to 'AddPrefixRoute'", info!!.description)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("PrefixRoute", highlightElement!!.text)
  }


}
