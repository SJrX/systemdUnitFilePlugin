package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseHtbClassRateOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // language="unit file (systemd)"
    val file = """
      [HierarchyTokenBucketClass]
      Rate=1M
      CeilRate=1M
      Rate=0.5M
      CeilRate=0.5M
      Rate=1G
      CeilRate=1G
      Rate=100G
      CeilRate=100G
      Rate=1G 500M
      CeilRate=1G 500M
      Rate=3.5G3B
      CeilRate=3.5G3B
      Rate=4T3G3B
      CeilRate=4T3G3B
      Rate=10.M
      CeilRate=10.M
      Rate=125000
      CeilRate=125000
      Rate=+5K
      CeilRate=+5K
      Rate=1000 B
      CeilRate=1000 B
      Rate=
      CeilRate=
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
      [HierarchyTokenBucketClass]
      Rate=10Mbit
      CeilRate=10Mbit
      Rate=1k
      CeilRate=1k
      Rate=-1M
      CeilRate=-1M
      Rate=3B3.5G
      CeilRate=3B3.5G
      Rate=12P12P
      CeilRate=12P12P
      Rate=1M 1M
      CeilRate=1M 1M
      Rate=abc
      CeilRate=abc
      Rate=.5M
      CeilRate=.5M
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(16, highlights)
  }

}
