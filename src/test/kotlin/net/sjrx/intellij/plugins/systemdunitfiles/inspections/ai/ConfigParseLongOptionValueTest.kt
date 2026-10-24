package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseLongOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // safe_atoli: signed long, base 0 plus the 0b/0o prefixes mangle_base adds.
    // language="unit file (systemd)"
    val file = """
      [Socket]
      MessageQueueMaxMessages=10
      MessageQueueMaxMessages=0
      MessageQueueMaxMessages=-5
      MessageQueueMaxMessages=+5
      MessageQueueMaxMessages=0x10
      MessageQueueMaxMessages=-0x10
      MessageQueueMaxMessages=010
      MessageQueueMaxMessages=0b101
      MessageQueueMaxMessages=0o17
      MessageQueueMaxMessages=9223372036854775807
      MessageQueueMaxMessages=-9223372036854775808
    """.trimIndent()

    setupFileInEditor("file.socket", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // language="unit file (systemd)"
    val file = """
      [Socket]
      MessageQueueMaxMessages=ten
      MessageQueueMaxMessages=08
      MessageQueueMaxMessages=0x
      MessageQueueMaxMessages=9223372036854775808
      MessageQueueMaxMessages=-0b1
      MessageQueueMaxMessages=10K
      MessageQueueMaxMessages=1.5
    """.trimIndent()

    setupFileInEditor("file.socket", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(7, highlights)
  }
}
