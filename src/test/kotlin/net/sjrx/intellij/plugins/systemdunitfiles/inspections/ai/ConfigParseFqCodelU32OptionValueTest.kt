package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseFqCodelU32OptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // sd-check at 0f0682b773 accepts these: strtoul starts afresh after the 0b/0o prefix, so a sign or blank may follow it.
    // language="unit file (systemd)"
    val file = """
      [FairQueueingControlledDelay]
      PacketLimit=0
      Flows=0
      PacketLimit=1
      Flows=1
      PacketLimit=20480
      Flows=20480
      PacketLimit=4294967295
      Flows=4294967295
      PacketLimit=0x10
      Flows=0x10
      PacketLimit=0o17
      Flows=0o17
      PacketLimit=0b101
      Flows=0b101
      PacketLimit=017
      Flows=017
      PacketLimit=+5
      Flows=+5
      PacketLimit=-0
      Flows=-0
      PacketLimit=0b+1
      Flows=0b+1
      PacketLimit=0b-0
      Flows=0b-0
      PacketLimit=0b 1
      Flows=0b 1
      PacketLimit=0o 7
      Flows=0o 7
      PacketLimit=0b +1
      Flows=0b +1
      PacketLimit=0b -0
      Flows=0b -0
      PacketLimit=
      Flows=
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
      [FairQueueingControlledDelay]
      PacketLimit=4294967296
      Flows=4294967296
      PacketLimit=-1
      Flows=-1
      PacketLimit=08
      Flows=08
      PacketLimit=0x
      Flows=0x
      PacketLimit=1.5
      Flows=1.5
      PacketLimit=abc
      Flows=abc
      PacketLimit=+0b1
      Flows=+0b1
      PacketLimit=12 13
      Flows=12 13
      PacketLimit=0b-1
      Flows=0b-1
      PacketLimit=0b -1
      Flows=0b -1
      PacketLimit=-0b0
      Flows=-0b0
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(22, highlights)
  }

}
