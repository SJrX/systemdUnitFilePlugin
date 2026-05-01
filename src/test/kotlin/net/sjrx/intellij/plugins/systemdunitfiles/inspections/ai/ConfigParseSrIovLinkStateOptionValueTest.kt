package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseSrIovLinkStateOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [SR-IOV]
      LinkState=auto
      LinkState=yes
      LinkState=no
      LinkState=true
      LinkState=false
      LinkState=on
      LinkState=off
      LinkState=1
      LinkState=0
    """.trimIndent()

    setupFileInEditor("file.link", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()
    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [SR-IOV]
      LinkState=invalid_value_1
      LinkState=enable
      LinkState=disable
    """.trimIndent()

    setupFileInEditor("file.link", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()
    assertSize(3, highlights)
  }
}
