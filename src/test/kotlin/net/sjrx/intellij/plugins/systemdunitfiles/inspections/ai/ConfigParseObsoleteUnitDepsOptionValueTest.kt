package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

/**
 * RequiresOverridable=/RequisiteOverridable= (config_parse_obsolete_unit_deps) forward to
 * config_parse_unit_deps, so they share the unit-dependency validator with Requires=/Requisite=.
 */
class ConfigParseObsoleteUnitDepsOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // language="unit file (systemd)"
    val file = """
      [Unit]
      RequiresOverridable=foo.service
      RequiresOverridable=foo.service bar.socket
      RequisiteOverridable=network-online.target
      RequisiteOverridable=getty@tty1.service
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
      [Unit]
      RequiresOverridable=foo
      RequisiteOverridable=foo.notatype
    """.trimIndent()

    setupFileInEditor("file.service", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(2, highlights)
  }
}
