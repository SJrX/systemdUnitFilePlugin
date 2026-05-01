package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDnsOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidSingleIpv4() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      DNS=192.168.1.1
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testValidMultipleIpv4() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      DNS=192.168.1.1 8.8.8.8 1.1.1.1
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testValidIpv6() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      DNS=2001:4860:4860::8888
      DNS=::1
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testValidMixedIpv4AndIpv6() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      DNS=192.168.1.1 2001:4860:4860::8888 8.8.4.4
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testValidIpv4WithPort() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      DNS=192.168.1.1:53
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testValidIpv4WithInterface() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      DNS=192.168.1.1%eth0
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testValidIpv6BracketedWithPort() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      DNS=[2001:4860:4860::8888]:53
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidIpv4OctetTooLarge() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      DNS=192.168.1.256
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(1, highlights)
  }

  @Test
  fun testInvalidHostname() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      DNS=dns.example.com
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(1, highlights)
  }

  @Test
  fun testInvalidGarbage() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      DNS=not-an-ip
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(1, highlights)
  }
}
