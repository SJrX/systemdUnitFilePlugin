package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseCakeFlowIsolationModeOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // language="unit file (systemd)"
    val file = """
      [CAKE]
      FlowIsolationMode=none
      FlowIsolationMode=src-host
      FlowIsolationMode=dst-host
      FlowIsolationMode=hosts
      FlowIsolationMode=flows
      FlowIsolationMode=dual-src-host
      FlowIsolationMode=dual-dst-host
      FlowIsolationMode=triple
      FlowIsolationMode=
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
      [CAKE]
      FlowIsolationMode=None
      FlowIsolationMode=dual
      FlowIsolationMode=triple-isolate
      FlowIsolationMode=src_host
      FlowIsolationMode=yes
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(5, highlights)
  }

}
