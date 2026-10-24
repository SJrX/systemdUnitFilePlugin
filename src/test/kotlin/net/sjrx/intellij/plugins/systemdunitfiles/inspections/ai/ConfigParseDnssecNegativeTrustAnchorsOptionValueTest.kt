package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDnssecNegativeTrustAnchorsOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      DNSSECNegativeTrustAnchors=example.com
      DNSSECNegativeTrustAnchors=test local. corp.example
      DNSSECNegativeTrustAnchors=.
      DNSSECNegativeTrustAnchors=_tcp.example
      DNSSECNegativeTrustAnchors=
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
      DNSSECNegativeTrustAnchors=a..b
      DNSSECNegativeTrustAnchors=good.example ..
      DNSSECNegativeTrustAnchors=~example.com..
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(3, highlights)
  }
}
