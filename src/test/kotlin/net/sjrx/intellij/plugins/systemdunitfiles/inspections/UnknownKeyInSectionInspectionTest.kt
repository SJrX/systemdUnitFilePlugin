package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class UnknownKeyInSectionInspectionTest : AbstractUnitFileTest() {
  fun testValidFileHasNoErrors() {
    // Fixture Setup
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
    val file = """
           [Service]
           CPUWeight=1
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
    val file = """
           [Slice]
           CPUWeight=1
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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

  fun testNSpawnFileTypeThrowsWarningWithKeyFromServiceFile() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Exec]
           BusName=yes
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.nspawn", file)

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

  // I think this test was not finished
  fun testNSpawnFileTypeHasNoWarningsWithKnownKey() {
    // Fixture Setup
    // language="unit file (systemd)"
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

  fun testNetDevShowsNoWarnings() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [NetDev]
           Name=wg0
           Kind=wireguard
          
           [WireGuard]
           PrivateKey=EEGlnEPYJV//kbvvIqxKkQwOiS+UENyPncC4bF46ong=
           ListenPort=51820
            
           [WireGuardPeer]
           PublicKey=RDf+LSpeEre7YEIKaxg+wbpsNV7du+ktR99uBEtIiCA=
           AllowedIPs=fd31:bf08:57cb::/48,192.168.26.0/24
           Endpoint=wireguard.example.com:51820
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.netdev", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertEmpty(highlights)
  }

  fun testNetDevShowsWarningWithUnknownKey() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [NetDev]
           Name=wg0
           Kind=wireguard
          
           [WireGuard]
           TLSEndpoint=tcp://64.45.45.12
                       
           [WireGuardPeer]
           PublicKey=RDf+LSpeEre7YEIKaxg+wbpsNV7du+ktR99uBEtIiCA=
           AllowedIPs=fd31:bf08:57cb::/48,192.168.26.0/24
           Endpoint=wireguard.example.com:51820
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.netdev", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("TLSEndpoint", highlightElement!!.text)
  }

  fun testNetworkFileShowsNoWarnings() {
    // Fixture Setup
    // language="unit file (systemd)"
    enableInspection(UnknownKeyInSectionInspection::class.java)
    val file = """
      # /etc/systemd/network/25-bridge-static.network
      [Match]
      Name=bridge0
      
      [Network]
      Address=192.168.0.15/24
      Gateway=192.168.0.1
      DNS=192.168.0.1
           """.trimIndent()
    setupFileInEditor("some.network", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertEmpty(highlights)
  }

  fun testNetworkShowsWarningWithUnknownKey() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """         
          # /etc/systemd/network/25-bridge-static.network
          [Match]
          NameAlias=bridge0
          
          [Network]
          Address=192.168.0.15/24
          Gateway=192.168.0.1
          DNS=192.168.0.1
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.network", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("NameAlias", highlightElement!!.text)
  }


  fun testLinkFileShowsNoWarnings() {
    // Fixture Setup
    // language="unit file (systemd)"
    enableInspection(UnknownKeyInSectionInspection::class.java)
    val file = """
      [Match]
      MACAddress=12:34:56:78:9a:bc
      Driver=brcmsmac
      Path=pci-0000:02:00.0-*
      Type=wlan
      Virtualization=no
      Host=my-laptop
      Architecture=x86-64
      
      [Link]
      Name=wireless0
      MTUBytes=1450
      BitsPerSecond=10M
      WakeOnLan=magic
      MACAddress=cb:a9:87:65:43:21
           """.trimIndent()
    setupFileInEditor("some.link", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertEmpty(highlights)
  }

  fun testLinkShowsWarningWithUnknownKey() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """         
          [Match]
          MACAddress=12:34:56:78:9a:bc
          Driver=brcmsmac
          Path=pci-0000:02:00.0-*
          Type=wlan
          Virtualization=no
          VirtualizationKind=qemu
          Host=my-laptop
          Architecture=x86-64
          
          [Link]
          Name=wireless0
          MTUBytes=1450
          BitsPerSecond=10M
          WakeOnLan=magic
          MACAddress=cb:a9:87:65:43:21
           """.trimIndent()
    enableInspection(UnknownKeyInSectionInspection::class.java)
    setupFileInEditor("some.link", file)

    // Exercise SUT
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    TestCase.assertEquals(UnknownKeyInSectionInspection.INSPECTION_TOOL_TIP_TEXT, info!!.description)
    TestCase.assertEquals(HighlightInfoType.WARNING, info.type)
    val highlightElement = myFixture.file.findElementAt(info.getStartOffset())
    TestCase.assertEquals("VirtualizationKind", highlightElement!!.text)
  }




  fun testSomeNewKeysFromSystemdV240HasNoWarnings() {
    // Fixture Setup
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
    // language="unit file (systemd)"
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
