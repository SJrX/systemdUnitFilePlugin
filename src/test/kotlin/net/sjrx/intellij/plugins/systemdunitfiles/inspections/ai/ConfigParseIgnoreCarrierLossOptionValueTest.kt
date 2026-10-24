package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseIgnoreCarrierLossOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      IgnoreCarrierLoss=true
      IgnoreCarrierLoss=false
      IgnoreCarrierLoss=No
      IgnoreCarrierLoss=5s
      IgnoreCarrierLoss=1min 30s
      IgnoreCarrierLoss=infinity
      IgnoreCarrierLoss=0
      IgnoreCarrierLoss=
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
      IgnoreCarrierLoss=sometimes
      IgnoreCarrierLoss=5 parsecs
      IgnoreCarrierLoss=-1s
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(3, highlights)
  }
}
