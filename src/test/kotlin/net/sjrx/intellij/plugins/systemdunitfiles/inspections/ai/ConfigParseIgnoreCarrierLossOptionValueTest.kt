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
      IgnoreCarrierLoss=Yes
      IgnoreCarrierLoss=TRUE
      IgnoreCarrierLoss=oFf
      IgnoreCarrierLoss=5s
      IgnoreCarrierLoss=1min 30s
      IgnoreCarrierLoss=infinity
      IgnoreCarrierLoss=0
      IgnoreCarrierLoss=1h30min
      IgnoreCarrierLoss=55s500ms
      IgnoreCarrierLoss=.5s
      IgnoreCarrierLoss=5s.5
      IgnoreCarrierLoss=+3.1s
      IgnoreCarrierLoss=3.1 sec .2 sec
      IgnoreCarrierLoss=2.5
      IgnoreCarrierLoss=23µs
      IgnoreCarrierLoss=5 min
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
      IgnoreCarrierLoss=5.
      IgnoreCarrierLoss=12.34.56
      IgnoreCarrierLoss=3.s
      IgnoreCarrierLoss=3. 1s
      IgnoreCarrierLoss=..1234
      IgnoreCarrierLoss=5ns
      IgnoreCarrierLoss=infinity .7
      IgnoreCarrierLoss=5+5
      IgnoreCarrierLoss=Infinity
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(12, highlights)
  }
}
