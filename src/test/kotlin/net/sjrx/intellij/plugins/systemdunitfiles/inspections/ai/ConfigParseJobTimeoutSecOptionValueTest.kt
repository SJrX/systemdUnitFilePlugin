package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseJobTimeoutSecOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // parse_sec_fix_0: any parse_sec time span, 0 meaning infinity. No specifier expansion.
    // language="unit file (systemd)"
    val file = """
      [Unit]
      JobTimeoutSec=infinity
      JobTimeoutSec=0
      JobTimeoutSec=90
      JobTimeoutSec=5min 20s
      JobTimeoutSec=1h30min
      JobTimeoutSec=.5s
      JobTimeoutSec=+10
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [Unit]
      JobTimeoutSec=-1
      JobTimeoutSec=5mins
      JobTimeoutSec=forever
      JobTimeoutSec=%i
      JobTimeoutSec=5.
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(5, highlights)
  }
}
