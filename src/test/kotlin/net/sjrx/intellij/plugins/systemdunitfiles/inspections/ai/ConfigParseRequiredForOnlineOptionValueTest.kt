package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseRequiredForOnlineOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [Link]
      RequiredForOnline=routable
      RequiredForOnline=no
      RequiredForOnline=yes
      RequiredForOnline=False
      RequiredForOnline=degraded-carrier
      RequiredForOnline=carrier:routable
      RequiredForOnline=off:
      RequiredForOnline=:degraded
      RequiredForOnline=no-carrier
      RequiredForOnline=enslaved
      RequiredForOnline=
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
      [Link]
      RequiredForOnline=online
      RequiredForOnline=:
      RequiredForOnline=routable:bogus
      RequiredForOnline=carrier:routable:off
      RequiredForOnline=Routable
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(5, highlights)
  }
}
