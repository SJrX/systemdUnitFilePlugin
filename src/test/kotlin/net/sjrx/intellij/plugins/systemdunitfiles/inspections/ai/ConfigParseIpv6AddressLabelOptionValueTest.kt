package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** [IPv6AddressLabel] Label=. */
class ConfigParseIpv6AddressLabelOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [IPv6AddressLabel]
            Label=0
            Label=100
            Label=4294967294
            Label=0xfffffffe
            Label=0b 1
            Label=0b+1
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidValues() {
    // One highlight per line. sd-check rejects +0b1 and -0b0 (EINVAL) and accepts 0b 1 and 0b+1.
    // language="unit file (systemd)"
    val file = """
            [IPv6AddressLabel]
            Label=4294967295
            Label=0xffffffff
            Label=-1
            Label=label
            Label=+0b1
            Label=-0b0
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(6, myFixture.doHighlighting())
  }
}
