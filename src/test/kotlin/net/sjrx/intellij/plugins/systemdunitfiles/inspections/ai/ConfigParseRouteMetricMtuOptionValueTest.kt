package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** [Route] MTUBytes= (config_parse_mtu, AF_UNSPEC). */
class ConfigParseRouteMetricMtuOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [Route]
            MTUBytes=1480
            MTUBytes=9000
            MTUBytes=0
            MTUBytes=1K
            MTUBytes=1.5K
            MTUBytes=1 K
            MTUBytes=1K 512
            MTUBytes=4294967295
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
            MTUBytes=4G
            MTUBytes=1500X
            MTUBytes=512 1K
            MTUBytes=-1
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(4, myFixture.doHighlighting())
  }
}
