package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/** config_parse_match_property: NAME=VALUE words with CUNESCAPE|UNQUOTE. */
class ConfigParseMatchPropertyOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValuesNetwork() {
    // language="unit file (systemd)"
    val file = """
            [Match]
            Property=ID_MODEL_ID=9999 "ID_VENDOR_FROM_DATABASE=vendor name" "KEY=with \"quotation\""
            Property=HOGE=foo BAR=baz
            Property=ID_NET_MANAGED_BY=io.systemd.Network
            Property=!ID_BUS=usb
            Property=! ID_BUS=usb
            Property=!
            Property=EMPTY=
            Property=KEY="value in quotes"
            Property=_UNDERSCORE1=x\x41
            Property=GLOB=foo*
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidValuesNetwork() {
    // One highlight per line.
    // language="unit file (systemd)"
    val file = """
            [Match]
            Property=NOEQUALS
            Property=1BAD=x
            Property=BAD-NAME=x
            Property="KEY=unterminated
            Property=KEY=bad\qescape
            Property=GOOD=1 !BAD=2
        """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(6, myFixture.doHighlighting())
  }
  @Test
  fun testValidValuesLink() {
    // language="unit file (systemd)"
    val file = """
            [Match]
            Property=ID_PATH=pci-0000:00:1f.6
        """.trimIndent()

    setupFileInEditor("file.link", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(0, myFixture.doHighlighting())
  }

  @Test
  fun testInvalidValuesLink() {
    // One highlight per line.
    // language="unit file (systemd)"
    val file = """
            [Match]
            Property==value
        """.trimIndent()

    setupFileInEditor("file.link", file)
    enableInspection(InvalidValueInspection::class.java)
    assertSize(1, myFixture.doHighlighting())
  }
}
