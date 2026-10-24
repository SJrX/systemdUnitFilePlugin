package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** [Route] InitialCongestionWindow=. */
class ConfigParseRouteMetricInitCwndOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // safe_atou32 sees the whole value, so whitespace or a sign after a 0b/0o prefix is fine (sd-check accepts 0b 1, 0b+1).
    // language="unit file (systemd)"
    val file = """
            [Route]
            InitialCongestionWindow=20
            InitialCongestionWindow=1
            InitialCongestionWindow=1023
            InitialCongestionWindow=0x10
            InitialCongestionWindow=0b 1
            InitialCongestionWindow=0b+1
            InitialCongestionWindow=0o 7
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidValues() {
    // One highlight per line. +0b1 and -0b0 are EINVAL: systemd turns off glibc's C23 0b parsing (sd-check rejects both).
    // language="unit file (systemd)"
    val file = """
            [Route]
            InitialCongestionWindow=0
            InitialCongestionWindow=1024
            InitialCongestionWindow=ten
            InitialCongestionWindow=+0b1
            InitialCongestionWindow=-0b0
            InitialCongestionWindow=08
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(6, myFixture.doHighlighting())
  }
}
