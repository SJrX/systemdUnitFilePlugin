package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseQfqMaxPacketOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // language="unit file (systemd)"
    val file = """
      [QuickFairQueueingClass]
      MaxPacketBytes=512
      MaxPacketBytes=16000
      MaxPacketBytes=8000
      MaxPacketBytes=65536
      MaxPacketBytes=64K
      MaxPacketBytes=1K
      MaxPacketBytes=0.5K
      MaxPacketBytes=1K 512B
      MaxPacketBytes=63K1023B
      MaxPacketBytes=
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
      [QuickFairQueueingClass]
      MaxPacketBytes=511
      MaxPacketBytes=65537
      MaxPacketBytes=65K
      MaxPacketBytes=1M
      MaxPacketBytes=0
      MaxPacketBytes=16000b
      MaxPacketBytes=abc
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(7, highlights)
  }

}
