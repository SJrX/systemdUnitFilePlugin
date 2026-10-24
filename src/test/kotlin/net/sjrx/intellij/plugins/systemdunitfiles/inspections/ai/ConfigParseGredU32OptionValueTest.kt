package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseGredU32OptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // sd-check at 0f0682b773 accepts these: strtoul starts afresh after the 0b/0o prefix, so a sign or blank may follow it.
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
      VirtualQueues=0b+1
      DefaultVirtualQueue=0b+1
      VirtualQueues=0b-0
      DefaultVirtualQueue=0b-0
      VirtualQueues=0b 1
      DefaultVirtualQueue=0b 1
      VirtualQueues=0o 7
      DefaultVirtualQueue=0o 7
      VirtualQueues=0b +1
      DefaultVirtualQueue=0b +1
      VirtualQueues=0b -0
      DefaultVirtualQueue=0b -0
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
    // sd-check at 0f0682b773 rejects these: 0b-1 and 0b -1 are -1 (ERANGE), -0b0 is -0 followed by junk (EINVAL).
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
      VirtualQueues=0b-1
      DefaultVirtualQueue=0b-1
      VirtualQueues=0b -1
      DefaultVirtualQueue=0b -1
      VirtualQueues=-0b0
      DefaultVirtualQueue=-0b0
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(18, highlights)
  }

}
