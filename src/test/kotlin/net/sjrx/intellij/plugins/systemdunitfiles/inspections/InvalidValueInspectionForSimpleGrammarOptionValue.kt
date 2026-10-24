package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValueInspectionForSimpleGrammarOptionValue : AbstractUnitFileTest() {

  fun testNoWarningWhenValidPortsSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file="""
      [FooOverUDP]
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
      # parse_ip_port() refuses 0 (sd-check: "Failed to parse Port=0, ignoring: Invalid argument"); this
      # was asserted valid before #568.
      Port=0
    """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.netdev", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(4, highlights)
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

  fun testNoWarningsWhenUnknownCapabilitiesAreSet() {
    // capability_set_from_string() only logs unknown names at debug level and skips them, so systemd accepts all of
    // these. The test used to assert four warnings (#568).
    // https://github.com/systemd/systemd/blob/0f0682b773/src/basic/capability-list.c#L160-L186
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
    assertSize(0, highlights)
  }


  fun testNoWarningWhenValidQuotasAreSet() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file="""
      [Service]
      StateDirectoryQuota=1K
      StateDirectoryQuota=1 M
      StateDirectoryQuota=1T
      StateDirectoryQuota=1 G
      StateDirectoryQuota=0
      StateDirectoryQuota=4294967295
      StateDirecotryQuota=1%
      StateDirecotryQuota=off
      StateDirecotryQuota=100%
      StateDirecotryQuota=0%
    """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }

  fun testWarningWhenInvalidQuotasAreSet() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file="""
      [Service]
      StateDirectoryQuota=-1
      StateDirectoryQuota=on
      StateDirectoryQuota=allo
      StateDirectoryQuota=500%
      StateDirectoryQuota=5.123%
    """.trimIndent()

    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    // 1P, 4294967296 and 5.52% used to be listed here, but config_parse_exec_quota() takes any parse_size() value and
    // parse_permyriad() allows two decimals (#568).
    assertSize(5, highlights)
  }

}
