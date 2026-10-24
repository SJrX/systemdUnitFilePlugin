package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseServiceRefreshOnReloadOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Booleans (parse_boolean is case-insensitive), resource lists, the ~ inversion (a bare ~ is an empty inverted list), and an empty reset.
    // language="unit file (systemd)"
    val file = """
      [Service]
      RefreshOnReload=yes
      RefreshOnReload=false
      RefreshOnReload=1
      RefreshOnReload=Yes
      RefreshOnReload=TRUE
      RefreshOnReload=oN
      RefreshOnReload=OFF
      RefreshOnReload=extensions
      RefreshOnReload=credentials
      RefreshOnReload=extensions credentials
      RefreshOnReload=~credentials
      RefreshOnReload=~ extensions
      RefreshOnReload=~
      RefreshOnReload=
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [Service]
      RefreshOnReload=~yes
      RefreshOnReload=Extensions
      RefreshOnReload=extensions,credentials
      RefreshOnReload=~~extensions
      RefreshOnReload=extensions ~credentials
      RefreshOnReload=all
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(6, highlights)
  }
}
