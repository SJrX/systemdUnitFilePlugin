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
            [IPv6AddressLabel]
            Label=4294967295
            Label=0xffffffff
            Label=-1
            Label=label
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(4, myFixture.doHighlighting())
  }
}
