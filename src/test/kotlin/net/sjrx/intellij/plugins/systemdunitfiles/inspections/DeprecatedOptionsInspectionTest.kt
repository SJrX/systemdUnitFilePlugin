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
           MemoryLimit=8
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(DeprecatedOptionsInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals("'MemoryLimit' in section 'Service' has been renamed to 'MemoryMax'", info!!.description)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertNotNull(highlightElement)
    TestCase.assertEquals("MemoryLimit", highlightElement!!.text)
  }

  fun testAllDocumentedDeprecatedOptionsInServiceAsOfV240ThrowsWarning() {
    val file = """
           [Service]
           CPUShares=52
           StartupCPUShares=25
           MemoryLimit=8
           BlockIOAccounting=1
           BlockIOWeight=25
           StartupBlockIOWeight=25
           BlockIODeviceWeight=25
           BlockIOReadBandwidth=2552
           BlockIOWriteBandwidth=252
           
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.service", file)
    enableInspection(DeprecatedOptionsInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(9, highlights)
  }

  fun testAllDocumentedDeprecatedOptionsInMountAsOfV240ThrowsWarning() {
    val file = """
           [Mount]
           CPUShares=52
           StartupCPUShares=25
           MemoryLimit=8
           BlockIOAccounting=1
           BlockIOWeight=25
           StartupBlockIOWeight=25
           BlockIODeviceWeight=25
           BlockIOReadBandwidth=2552
           BlockIOWriteBandwidth=252
           
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.mount", file)
    enableInspection(DeprecatedOptionsInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(9, highlights)
  }

  fun testAllDocumentedDeprecatedOptionsInSocketAsOfV240ThrowsWarning() {
    val file = """
           [Socket]
           CPUShares=52
           StartupCPUShares=25
           MemoryLimit=8
           BlockIOAccounting=1
           BlockIOWeight=25
           StartupBlockIOWeight=25
           BlockIODeviceWeight=25
           BlockIOReadBandwidth=2552
           BlockIOWriteBandwidth=252
           
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.socket", file)
    enableInspection(DeprecatedOptionsInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(9, highlights)
  }

  fun testAllDocumentedDeprecatedOptionsInSwapAsOfV240ThrowsWarning() {
    val file = """
           [Swap]
           CPUShares=52
           StartupCPUShares=25
           MemoryLimit=8
           BlockIOAccounting=1
           BlockIOWeight=25
           StartupBlockIOWeight=25
           BlockIODeviceWeight=25
           BlockIOReadBandwidth=2552
           BlockIOWriteBandwidth=252
           
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.swap", file)
    enableInspection(DeprecatedOptionsInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(9, highlights)
  }

  fun testAllDocumentedDeprecatedOptionsInSliceAsOfV240ThrowsWarning() {
    val file = """
           [Slice]
           CPUShares=52
           StartupCPUShares=25
           MemoryLimit=8
           BlockIOAccounting=1
           BlockIOWeight=25
           StartupBlockIOWeight=25
           BlockIODeviceWeight=25
           BlockIOReadBandwidth=2552
           BlockIOWriteBandwidth=252
           
           """.trimIndent()

    // Exercise SUT
    setupFileInEditor("file.slice", file)
    enableInspection(DeprecatedOptionsInspection::class.java)

    // Verification
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(9, highlights)
  }
}
