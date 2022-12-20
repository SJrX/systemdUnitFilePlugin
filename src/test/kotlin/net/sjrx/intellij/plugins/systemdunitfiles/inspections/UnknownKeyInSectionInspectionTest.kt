package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class UnknownKeyInSectionInspectionTest : AbstractUnitFileTest() {
  fun testValidFileHasNoErrors() {
    // Fixture Setup
    val file = """
           [Unit]
           Description=Hello Good Sir
           [Install]
           Alias=Foo
           [Service]
           SuccessExitStatus=5
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("file.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertEmpty(highlights)
  }

  fun testUnknownKeyInUnitSectionGeneratesWarning() {

    // Fixture Setup
    val file = """
           [Unit]
           BadKey=Hello Good Sir
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("file.service", file)


    // Exercise SUT
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("BadKey", highlightElement!!.text)
  }

  fun testUnknownKeyInInstallSectionGeneratesWarning() {
    // Fixture Setup
    val file = """
           [Install]
           BadKey=Hello Good Sir
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("file.service", file)


    // Exercise SUT
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("BadKey", highlightElement!!.text)
  }

  fun testUnknownKeyInServiceSectionGeneratesWarning() {
    // Fixture Setup
    val file = """
           [Service]
           BadKey=Hello Good Sir
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("file.service", file)


    // Exercise SUT
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("BadKey", highlightElement!!.text)
  }

  fun testTwoUnknownKeysInSameSectionReturnError() {
    // Fixture Setup
    val file = """
           [Service]
           BadKey=Hello Good Sir
           BadKeyThree=Hello Good Sir
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("file.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(2, highlights)
    var info = highlights[0]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    var highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("BadKey", highlightElement!!.text)
    info = highlights[1]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("BadKeyThree", highlightElement!!.text)
  }

  fun testTwoUnknownKeysInDistinctSectionsReturnErrors() {
    // Fixture Setup
    val file = """
           [Service]
           BadKey=Hello Good Sir
           [Unit]
           BadKeyThree=Hello Good Sir
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("file.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(2, highlights)
    var info = highlights[0]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    var highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("BadKey", highlightElement!!.text)
    info = highlights[1]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("BadKeyThree", highlightElement!!.text)
  }

  fun testKeyStartingWithXDashDoesNotReturnError() {
    // Fixture Setup
    val file = """
           [Service]
           X-BadKey=Hello Good Sir
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("file.service", file)


    // Exercise SUT
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(0, highlights)
  }

  fun testSectionStartingWithXDashAndBadKeysDoNotCauseError() {
    // Fixture Setup
    val file = """
           [X-Service]
           BadKey=Hello Good Sir
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("file.service", file)


    // Exercise SUT
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(0, highlights)
  }

  fun testKeyFromInstallSectionThrowsWarningInUnitSection() {
    // Fixture Setup
    val file = """
           [Unit]
           Alias=Hello Good Sir
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("file.service", file)


    // Exercise SUT
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("Alias", highlightElement!!.text)
  }

  fun testKeyFromUnitSectionThrowsWarningInInstallSection() {
    // Fixture Setup
    val file = """
           [Install]
           Requires=Hello Good Sir
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("file.service", file)


    // Exercise SUT
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("Requires", highlightElement!!.text)
  }

  fun testKeyFromInstallSectionThrowsWarningInServiceSection() {
    // Fixture Setup
    val file = """
           [Service]
           Alias=Hello Good Sir
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("file.service", file)


    // Exercise SUT
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("Alias", highlightElement!!.text)
  }

  fun testKeyFromUnitSectionThrowsWarningInServiceSection() {
    // Fixture Setup
    val file = """
           [Service]
           Requires=Hello Good Sir
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("file.service", file)


    // Exercise SUT
    val highlights = myFixture.doHighlighting()


    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("Requires", highlightElement!!.text)
  }

  fun testKeyFromResourceControlManPageDoesNotThrowWarningInServiceSection() {
    // Fixture Setup
    val file = """
           [Service]
           CPUAccounting=on
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("file.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertEmpty(highlights)
  }

  fun testKeyFromExecManPageDoesNotThrowWarningInServiceSection() {
    // Fixture Setup
    val file = """
           [Service]
           DynamicUser=yes
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("file.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertEmpty(highlights)
  }

  fun testAutomountFileTypeHasNoWarningsWithKnownKey() {
    // Fixture Setup
    val file = """
           [Automount]
           Where=yes
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.automount", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertEmpty(highlights)
  }

  fun testAutomountFileTypeThrowsWarningWithKeyFromServiceFile() {
    // Fixture Setup
    val file = """
           [Automount]
           BusName=yes
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.automount", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("BusName", highlightElement!!.text)
  }

  fun testDeviceFileTypeHasNoWarningsWithKnownKey() {
    // Fixture Setup
    val file = """
           [Unit]
           Description=SomeUnit
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.device", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertEmpty(highlights)
  }

  fun testDeviceFileTypeThrowsWarningWithKeyFromServiceFile() {
    // Fixture Setup
    val file = """
           [Unit]
           BusName=yes
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.device", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("BusName", highlightElement!!.text)
  }

  fun testMountFileTypeHasNoWarningsWithKnownKey() {
    // Fixture Setup
    val file = """
           [Mount]
           SloppyOptions=true
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.mount", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertEmpty(highlights)
  }

  fun testMountFileTypeThrowsWarningWithKeyFromServiceFile() {
    // Fixture Setup
    val file = """
           [Mount]
           BusName=yes
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.mount", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("BusName", highlightElement!!.text)
  }

  fun testPathFileTypeHasNoWarningsWithKnownKey() {
    // Fixture Setup
    val file = """
           [Path]
           MakeDirectory=true
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.path", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertEmpty(highlights)
  }

  fun testPathFileTypeThrowsWarningWithKeyFromServiceFile() {
    // Fixture Setup
    val file = """
           [Path]
           BusName=yes
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.path", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("BusName", highlightElement!!.text)
  }

  fun testServiceFileTypeHasNoWarningsWithKnownKey() {
    // Fixture Setup
    val file = """
           [Service]
           BusName=true
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertEmpty(highlights)
  }

  fun testServiceFileTypeThrowsWarningWithKeyFromPathFile() {
    // Fixture Setup
    val file = """
           [Service]
           MakeDirectory=yes
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("MakeDirectory", highlightElement!!.text)
  }

  fun testSliceFileTypeHasNoWarningsWithKnownKey() {
    // Fixture Setup
    val file = """
           [Slice]
           CPUAccounting=true
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.slice", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertEmpty(highlights)
  }

  fun testSliceFileTypeThrowsWarningWithKeyFromUnitSection() {
    // Fixture Setup
    val file = """
           [Slice]
           Description=yes
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.slice", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("Description", highlightElement!!.text)
  }

  fun testSocketFileTypeHasNoWarningsWithKnownKey() {
    // Fixture Setup
    val file = """
           [Socket]
           Backlog=5
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.socket", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertEmpty(highlights)
  }

  fun testSocketFileTypeThrowsWarningWithKeyFromServiceFile() {
    // Fixture Setup
    val file = """
           [Socket]
           BusName=yes
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.socket", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("BusName", highlightElement!!.text)
  }

  fun testSwapFileTypeHasNoWarningsWithKnownKey() {
    // Fixture Setup
    val file = """
           [Swap]
           Priority=5
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.swap", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertEmpty(highlights)
  }

  fun testSwapFileTypeThrowsWarningWithKeyFromServiceFile() {
    // Fixture Setup
    val file = """
           [Swap]
           BusName=yes
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.swap", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("BusName", highlightElement!!.text)
  }

  fun testTargetFileTypeHasNoWarningsWithKnownKey() {
    // Fixture Setup
    val file = """
           [Unit]
           Description=SomeUnit
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.target", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertEmpty(highlights)
  }

  fun testTargetFileTypeThrowsWarningWithKeyFromServiceFile() {
    // Fixture Setup
    val file = """
           [Unit]
           BusName=yes
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.target", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("BusName", highlightElement!!.text)
  }

  fun testTimerFileTypeHasNoWarningsWithKnownKey() {
    // Fixture Setup
    val file = """
           [Timer]
           RandomizedDelaySec=50
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.timer", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertEmpty(highlights)
  }

  fun testTimerFileTypeThrowsWarningWithKeyFromServiceFile() {
    // Fixture Setup
    val file = """
           [Timer]
           BusName=yes
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.timer", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("BusName", highlightElement!!.text)
  }

  fun testSomeNewKeysFromSystemdV240HasNoWarnings() {
    // Fixture Setup
    val file = """
           [Unit]
           FailureActionExitStatus=249
           [Service]
           Type=exec
           MemoryMin=2549M
           LogRateLimitIntervalSec=152
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("systemd-v240-smoke-test.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertEmpty(highlights)
  }

  fun testSomeNewKeysFromSystemdV246HasNoWarnings() {
    // Fixture Setup
    val file = """
           [Service]
           CoredumpFilter=bar
           RootHashSignature=true
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("systemd-v246-smoke-test.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertEmpty(highlights)
  }

  fun testThatMovedKeysFromServiceToUnitInSystemd229ThrowNoWarnings() {
    // Fixture Setup
    val file = """
           [Service]
           StartLimitBurst=24
           StartLimitInterval=24s
           StartLimitAction=none
           RebootArgument=yoyo
           
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("systemd-v229-moved.service", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertEmpty(highlights)
  }
}
