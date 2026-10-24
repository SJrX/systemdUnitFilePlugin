package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseGredU32OptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // language="unit file (systemd)"
    val file = """
      [GenericRandomEarlyDetection]
      VirtualQueues=12
      DefaultVirtualQueue=12
      VirtualQueues=10
      DefaultVirtualQueue=10
      VirtualQueues=0
      DefaultVirtualQueue=0
      VirtualQueues=16
      DefaultVirtualQueue=16
      VirtualQueues=0x10
      DefaultVirtualQueue=0x10
      VirtualQueues=0b1
      DefaultVirtualQueue=0b1
      VirtualQueues=020
      DefaultVirtualQueue=020
      VirtualQueues=+3
      DefaultVirtualQueue=+3
      VirtualQueues=
      DefaultVirtualQueue=
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
      [GenericRandomEarlyDetection]
      VirtualQueues=17
      DefaultVirtualQueue=17
      VirtualQueues=0x11
      DefaultVirtualQueue=0x11
      VirtualQueues=-1
      DefaultVirtualQueue=-1
      VirtualQueues=abc
      DefaultVirtualQueue=abc
      VirtualQueues=4294967296
      DefaultVirtualQueue=4294967296
      VirtualQueues=1.0
      DefaultVirtualQueue=1.0
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(12, highlights)
  }

}
