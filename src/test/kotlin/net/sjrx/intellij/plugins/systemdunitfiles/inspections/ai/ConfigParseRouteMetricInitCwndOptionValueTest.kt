package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** [Route] InitialCongestionWindow=. */
class ConfigParseRouteMetricInitCwndOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [Route]
            InitialCongestionWindow=20
            InitialCongestionWindow=1
            InitialCongestionWindow=1023
            InitialCongestionWindow=0x10
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidValues() {
    // One highlight per line.
    // language="unit file (systemd)"
    val file = """
            [Route]
            InitialCongestionWindow=0
            InitialCongestionWindow=1024
            InitialCongestionWindow=ten
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(3, myFixture.doHighlighting())
  }
}
