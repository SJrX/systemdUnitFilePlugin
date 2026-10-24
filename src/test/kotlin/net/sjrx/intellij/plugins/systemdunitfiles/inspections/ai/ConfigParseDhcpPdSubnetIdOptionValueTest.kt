package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDhcpPdSubnetIdOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // SubnetId= is always hexadecimal (safe_atoux64), so `ff` and `add` are numbers.
    // language="unit file (systemd)"
    val file = """
      [DHCPPrefixDelegation]
      SubnetId=auto
      SubnetId=0
      SubnetId=10
      SubnetId=ff
      SubnetId=0xabcd
      SubnetId=add
      SubnetId=7fffffffffffffff
      SubnetId=
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
      [DHCPPrefixDelegation]
      SubnetId=8000000000000000
      SubnetId=Auto
      SubnetId=xyz
      SubnetId=1g
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(4, highlights)
  }
}
