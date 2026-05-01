package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDnssecModeOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidTableValues() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      DNSSEC=yes
      DNSSEC=no
      DNSSEC=allow-downgrade
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testValidBooleanValues() {
    // language="unit file (systemd)"
    val file = """
      [Network]
      DNSSEC=true
      DNSSEC=false
      DNSSEC=on
      DNSSEC=off
      DNSSEC=1
      DNSSEC=0
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
      DNSSEC=invalid
      DNSSEC=allow
      DNSSEC=downgrade
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(3, highlights)
  }
}
