package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** config_parse_pref64_prefix_lifetime: at most 8191 * 8s, not infinity. */
class ConfigParsePref64PrefixLifetimeOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [IPv6PREF64Prefix]
            LifetimeSec=1800
            LifetimeSec=0
            LifetimeSec=65528
            LifetimeSec=18h 12min 8s
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
            [IPv6PREF64Prefix]
            LifetimeSec=65529
            LifetimeSec=infinity
            LifetimeSec=1d
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(3, myFixture.doHighlighting())
  }
}
