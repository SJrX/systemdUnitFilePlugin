package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValueInspectionForSimpleGrammarOptionValue : AbstractUnitFileTest() {

  fun testNoWarningWhenValidPortsSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file="""
      [FooOverUDP]
      Port=0
      Port=1
      Port=32768
      Port=65535
    """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.netdev", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningsWhenInvalidPortsSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file="""
      [FooOverUDP]
      Port=-1
      Port=a
      Port=65536

    """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.netdev", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(3, highlights)
  }

  fun testNoWarningWhenValidCoalescedUint32Specified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file="""
      [Link]
      RxMaxCoalescedFrames=0
      RxMaxCoalescedFrames=0x0
      RxMaxCoalescedFrames=0x1
      RxMaxCoalescedFrames=0xab
      RxMaxCoalescedFrames=0xAB
      RxMaxCoalescedFrames=0455
      RxMaxCoalescedFrames=4294967295
    """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.link", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningsWhenInvalidCoalescedUint32Specified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file="""
      [Link]
      RxMaxCoalescedFrames=-1      
      RxMaxCoalescedFrames=0xABZ
      RxMaxCoalescedFrames=4294967296
    """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.link", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(3, highlights)
  }

  fun testNoWarningValidCapabilitiesAreSet() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file="""
      [Service]
      CapabilityBoundingSet=CAP_SYS_ADMIN
      CapabilityBoundingSet=CAP_CHOWN
      CapabilityBoundingSet=CAP_DAC_OVERRIDE
      CapabilityBoundingSet=CAP_MKNOD CAP_NET_ADMIN CAP_NET_RAW
      CapabilityBoundingSet=~CAP_MAC_OVERRIDE
      CapabilityBoundingSet=~
    """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningsWhenInValidCapabilitiesAreSet() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file="""
      [Service]
      CapabilityBoundingSet=CAP_CHOWNER
      CapabilityBoundingSet=CAP_AUDIT_CONTROLCAP_SETGID
      CapabilityBoundingSet=HELLO
      CapabilityBoundingSet=CAP_SYS_BOOT ~CAP_SYS_TIME CAP_SYSLOG
    """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(4, highlights)
  }

}
