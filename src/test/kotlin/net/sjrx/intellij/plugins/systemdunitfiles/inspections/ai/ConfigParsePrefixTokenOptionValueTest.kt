package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** [IPv6Prefix] Token= (config_parse_prefix_token -> config_parse_address_generation_type). */
class ConfigParsePrefixTokenOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
            [IPv6Prefix]
            Token=eui64
            Token=::1a:2b:3c:4d
            Token=static:::fa:de:ca:fe
            Token=prefixstable
            Token=prefixstable:2002:da8:1::,86b123b969ba4b7eb8b3d8605123525a
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
            [IPv6Prefix]
            Token=prefixstable@
            Token=prefixstable,00000000000000000000000000000000
            Token=static:
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(3, myFixture.doHighlighting())
  }
}
