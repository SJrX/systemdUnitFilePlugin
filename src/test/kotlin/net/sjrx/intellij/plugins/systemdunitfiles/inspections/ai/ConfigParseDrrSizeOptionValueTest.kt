package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseDrrSizeOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // language="unit file (systemd)"
    val file = """
      [DeficitRoundRobinSchedulerClass]
      QuantumBytes=2000
      QuantumBytes=1K
      QuantumBytes=1.5K
      QuantumBytes=4095M
      QuantumBytes=4294967295
      QuantumBytes=3G 1023M
      QuantumBytes=
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
      [DeficitRoundRobinSchedulerClass]
      QuantumBytes=4G
      QuantumBytes=4294967296
      QuantumBytes=1k
      QuantumBytes=-1
      QuantumBytes=abc
      QuantumBytes=1K 1K
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(6, highlights)
  }

}
