package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseBfifoSizeOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // language="unit file (systemd)"
    val file = """
      [BFIFO]
      LimitBytes=1000000
      LimitBytes=1K
      LimitBytes=1M
      LimitBytes=1.5M
      LimitBytes=4095M
      LimitBytes=4294967295
      LimitBytes=3G 1023M
      LimitBytes=
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
      [BFIFO]
      LimitBytes=4G
      LimitBytes=4294967296
      LimitBytes=1k
      LimitBytes=-1
      LimitBytes=abc
      LimitBytes=1M 1M
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(6, highlights)
  }

}
