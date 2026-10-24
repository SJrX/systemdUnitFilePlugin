package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseTbfRateOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // language="unit file (systemd)"
    val file = """
      [TokenBucketFilter]
      Rate=1M
      PeakRate=1M
      Rate=0.5M
      PeakRate=0.5M
      Rate=1G
      PeakRate=1G
      Rate=100G
      PeakRate=100G
      Rate=1G 500M
      PeakRate=1G 500M
      Rate=3.5G3B
      PeakRate=3.5G3B
      Rate=4T3G3B
      PeakRate=4T3G3B
      Rate=10.M
      PeakRate=10.M
      Rate=125000
      PeakRate=125000
      Rate=+5K
      PeakRate=+5K
      Rate=1000 B
      PeakRate=1000 B
      Rate=
      PeakRate=
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
      [TokenBucketFilter]
      Rate=10Mbit
      PeakRate=10Mbit
      Rate=1k
      PeakRate=1k
      Rate=-1M
      PeakRate=-1M
      Rate=3B3.5G
      PeakRate=3B3.5G
      Rate=12P12P
      PeakRate=12P12P
      Rate=1M 1M
      PeakRate=1M 1M
      Rate=abc
      PeakRate=abc
      Rate=.5M
      PeakRate=.5M
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(16, highlights)
  }

}
