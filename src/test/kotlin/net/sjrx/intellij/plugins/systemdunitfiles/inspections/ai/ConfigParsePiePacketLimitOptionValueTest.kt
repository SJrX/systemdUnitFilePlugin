package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParsePiePacketLimitOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // language="unit file (systemd)"
    val file = """
      [PIE]
      PacketLimit=0
      PacketLimit=1
      PacketLimit=20480
      PacketLimit=4294967295
      PacketLimit=0x10
      PacketLimit=0o17
      PacketLimit=0b101
      PacketLimit=017
      PacketLimit=+5
      PacketLimit=-0
      PacketLimit=
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
      [PIE]
      PacketLimit=4294967296
      PacketLimit=-1
      PacketLimit=08
      PacketLimit=0x
      PacketLimit=1.5
      PacketLimit=abc
      PacketLimit=+0b1
      PacketLimit=12 13
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(8, highlights)
  }

}
