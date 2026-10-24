package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseIecSizeLongOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // parse_size(…, 1024) spellings from TEST(parse_size) (src/test/test-parse-util.c).
    // language="unit file (systemd)"
    val file = """
      [Socket]
      MessageQueueMessageSize=8192
      MessageQueueMessageSize=111.4
      MessageQueueMessageSize=112 B
      MessageQueueMessageSize=112.6 B
      MessageQueueMessageSize=3.5 K
      MessageQueueMessageSize=3. K
      MessageQueueMessageSize=3.0 K
      MessageQueueMessageSize=4 M 11.5K
      MessageQueueMessageSize=3.5G3B
      MessageQueueMessageSize=3.5G 4B
      MessageQueueMessageSize=4T3G3B
      MessageQueueMessageSize=4 T 3 G 3 B
      MessageQueueMessageSize=12P
      MessageQueueMessageSize=3E 2P
      MessageQueueMessageSize=+8K
      MessageQueueMessageSize=1K5
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
      MessageQueueMessageSize=3. 0 K
      MessageQueueMessageSize=12X
      MessageQueueMessageSize=12.5X
      MessageQueueMessageSize=12.5e3
      MessageQueueMessageSize=-1
      MessageQueueMessageSize=-10B 20K
      MessageQueueMessageSize=8k
      MessageQueueMessageSize=8 KiB
    """.trimIndent()

    setupFileInEditor("file.socket", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(8, highlights)
  }
}
