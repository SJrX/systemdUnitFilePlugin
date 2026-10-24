package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseLldpMulticastModeOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      EmitLLDP=nearest-bridge
      EmitLLDP=non-tpmr-bridge
      EmitLLDP=customer-bridge
      EmitLLDP=yes
      EmitLLDP=no
      EmitLLDP=On
      EmitLLDP=Yes
      EmitLLDP=TRUE
      EmitLLDP=oFf
      EmitLLDP=
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
      EmitLLDP=nearest
      EmitLLDP=Customer-Bridge
      EmitLLDP=routers-only
      EmitLLDP=Nearest-Bridge
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(4, highlights)
  }
}
