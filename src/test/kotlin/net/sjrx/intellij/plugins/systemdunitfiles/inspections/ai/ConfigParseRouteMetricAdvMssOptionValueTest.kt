package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** [Route] TCPAdvertisedMaximumSegmentSize=. */
class ConfigParseRouteMetricAdvMssOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [Route]
            TCPAdvertisedMaximumSegmentSize=30
            TCPAdvertisedMaximumSegmentSize=1460
            TCPAdvertisedMaximumSegmentSize=1K
            TCPAdvertisedMaximumSegmentSize=4294967295
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
            TCPAdvertisedMaximumSegmentSize=0
            TCPAdvertisedMaximumSegmentSize=4G
            TCPAdvertisedMaximumSegmentSize=big
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(3, myFixture.doHighlighting())
  }
}
