package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import junit.framework.TestCase
import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest

class InvalidValueInspectionForCGroupSocketBindOptionValue : AbstractUnitFileTest() {

  fun testNoWarningsWithDocumentedExamples() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           # Allow binding IPv6 socket addresses with a port greater than or equal to 10000.
           SocketBindAllow=ipv6:10000-65535
           SocketBindDeny=any
           # Allow binding IPv4 and IPv6 socket addresses with 1234 and 4321 ports.
           
           SocketBindAllow=1234
           SocketBindAllow=4321
           SocketBindDeny=any
           # Deny binding IPv6 socket addresses.
           SocketBindDeny=ipv6
           # Deny binding IPv4 and IPv6 socket addresses.
           
           SocketBindDeny=any
           
           # Allow binding only over TCP
           
           SocketBindAllow=tcp
           SocketBindDeny=any
           
           # Allow binding only over IPv6/TCP
           SocketBindAllow=ipv6:tcp
           SocketBindDeny=any
           
           # Allow binding ports within 10000-65535 range over IPv4/UDP.
           SocketBindAllow=ipv4:udp:10000-65535
           SocketBindDeny=any

           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(0, highlights)
  }


  fun testWeakWarningWhenInvalidAddressFamilySpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           SocketBindAllow=ipv5
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("SocketBindAllow's value does not match the expected format.", info!!.description)
    TestCase.assertEquals("ipv5", info.text)
  }


  fun testWeakWarningWhenInvalidTransportSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           SocketBindAllow=icmp
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("SocketBindAllow's value does not match the expected format.", info!!.description)
    TestCase.assertEquals("icmp", info.text)
  }


  fun testWeakWarningWhenInvalidPortSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           SocketBindAllow=12458757
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("SocketBindAllow's value is correctly formatted but seems invalid.", info!!.description)
    TestCase.assertEquals("12458757", info.text)
  }

  fun testWeakWarningWhenDoubleColonSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           SocketBindAllow=ipv6::tcp
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("SocketBindAllow's value does not match the expected format.", info!!.description)
    TestCase.assertEquals("::tcp", info.text)
  }

  fun testWeakWarningWhenInvalidPortRangeSpecified() {
    // Fixture Setup
    // language="unit file (systemd)"
    val file = """
           [Service]
           SocketBindAllow=ipv6:tcp:12--21485
           """.trimIndent()


    // Execute SUT
    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    // Verification
    assertSize(1, highlights)
    val info = highlights[0]
    assertStringContains("SocketBindAllow's value is correctly formatted but seems invalid.", info!!.description)
    TestCase.assertEquals("-", info.text)
  }
}
