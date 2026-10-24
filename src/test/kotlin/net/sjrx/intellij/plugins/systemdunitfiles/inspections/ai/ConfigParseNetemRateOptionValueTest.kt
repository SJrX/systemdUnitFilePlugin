package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseNetemRateOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // language="unit file (systemd)"
    val file = """
      [NetworkEmulator]
      LossRate=20%
      DuplicateRate=20%
      LossRate=0%
      DuplicateRate=0%
      LossRate=100%
      DuplicateRate=100%
      LossRate=55.5%
      DuplicateRate=55.5%
      LossRate=55.53%
      DuplicateRate=55.53%
      LossRate=555.5‰
      DuplicateRate=555.5‰
      LossRate=1000‰
      DuplicateRate=1000‰
      LossRate=1000‱
      DuplicateRate=1000‱
      LossRate=0x10%
      DuplicateRate=0x10%
      LossRate=
      DuplicateRate=
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
      [NetworkEmulator]
      LossRate=20
      DuplicateRate=20
      LossRate=107%
      DuplicateRate=107%
      LossRate=-7%
      DuplicateRate=-7%
      LossRate=3.212%
      DuplicateRate=3.212%
      LossRate=3.22‰
      DuplicateRate=3.22‰
      LossRate=3.2‱
      DuplicateRate=3.2‱
      LossRate=abc%
      DuplicateRate=abc%
      LossRate=%
      DuplicateRate=%
      LossRate=20 %
      DuplicateRate=20 %
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(18, highlights)
  }

}
