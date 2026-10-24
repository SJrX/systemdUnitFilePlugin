package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseFqUsecOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // language="unit file (systemd)"
    val file = """
      [FairQueueing]
      CEThresholdSec=100ms
      CEThresholdSec=1s
      CEThresholdSec=4294967295us
      CEThresholdSec=71min 34s
      CEThresholdSec=1h 11min 34.967295s
      CEThresholdSec=0
      CEThresholdSec=
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
      [FairQueueing]
      CEThresholdSec=4294967296us
      CEThresholdSec=72min
      CEThresholdSec=infinity
      CEThresholdSec=1h 12min
      CEThresholdSec=5x
      CEThresholdSec=-1s
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(6, highlights)
  }

}
