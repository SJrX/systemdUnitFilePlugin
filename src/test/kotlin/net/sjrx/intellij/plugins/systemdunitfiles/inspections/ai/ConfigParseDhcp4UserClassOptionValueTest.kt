package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDhcp4UserClassOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [DHCPv4]
      UserClass=foo
      UserClass=foo bar baz
      UserClass="with space" 'single quoted'
      UserClass=esc\x41ped tab\tbed
      UserClass=mid"dle quo"te
      UserClass="a"
      UserClass=
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
      [DHCPv4]
      UserClass="unterminated
      UserClass=bad\qescape
      UserClass=bad\ escaped-space
      UserClass=""
      UserClass=''
      UserClass=a ""
      UserClass="" b
      UserClass=""''
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(8, highlights)
  }
}
