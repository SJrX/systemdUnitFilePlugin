package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseNtpOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      NTP=0.fedora.pool.ntp.org 1.fedora.pool.ntp.org
      NTP=192.168.5.1
      NTP=2001:db8::123
      NTP=ntp.example.com.
      NTP=time
      NTP=.
      NTP=foo\.bar
      NTP=foo\qbar
      NTP=foo\065bar
      NTP=foo\\\\bar
      NTP=foo\\.bar
      NTP=foo\\065bar
      NTP=localhost.localdomain
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      NTP=pool..ntp.org
      NTP=.ntp.org
      NTP=ok.example bad..example
      NTP=foo\\bar
      NTP=foo\\256bar
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(5, highlights)
  }
}
