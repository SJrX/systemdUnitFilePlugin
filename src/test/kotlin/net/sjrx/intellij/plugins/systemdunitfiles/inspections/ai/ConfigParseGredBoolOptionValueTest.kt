package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseGredBoolOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // language="unit file (systemd)"
    val file = """
      [GenericRandomEarlyDetection]
      GenericRIO=yes
      GenericRIO=no
      GenericRIO=1
      GenericRIO=0
      GenericRIO=true
      GenericRIO=false
      GenericRIO=on
      GenericRIO=off
      GenericRIO=y
      GenericRIO=n
      GenericRIO=t
      GenericRIO=f
      GenericRIO=YES
      GenericRIO=False
      GenericRIO=On
      GenericRIO=Yes
      GenericRIO=TRUE
      GenericRIO=oFf
      GenericRIO=Y
      GenericRIO=
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
      [GenericRandomEarlyDetection]
      GenericRIO=maybe
      GenericRIO=2
      GenericRIO=enable
      GenericRIO=nope
      GenericRIO=Maybe
      GenericRIO=ENABLE
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(6, highlights)
  }

}
