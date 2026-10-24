package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDnsOverTlsModeOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      DNSOverTLS=opportunistic
      DNSOverTLS=yes
      DNSOverTLS=no
      DNSOverTLS=TRUE
      DNSOverTLS=1
      DNSOverTLS=Yes
      DNSOverTLS=oFf
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
      DNSOverTLS=Opportunistic
      DNSOverTLS=strict
      DNSOverTLS=allow-downgrade
      DNSOverTLS=OPPORTUNISTIC
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(4, highlights)
  }
}
