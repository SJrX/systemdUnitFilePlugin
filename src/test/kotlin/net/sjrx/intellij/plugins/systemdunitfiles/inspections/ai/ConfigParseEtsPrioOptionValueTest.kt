package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import org.junit.Test

class ConfigParseEtsPrioOptionValueTest : AbstractUnitFileTest() {

  @Test
  fun testValidValues() {
    // Fixture values from systemd test/test-network/conf plus tricky spellings the C parser accepts.
    // sd-check at 0f0682b773 accepts 0b+1 and 0b-0 (a sign may follow the 0b prefix).
    // language="unit file (systemd)"
    val file = """
      [EnhancedTransmissionSelection]
      PriorityMap=8 7 6 5
      PriorityMap=3 4 5
      PriorityMap=6 7
      PriorityMap=0
      PriorityMap=255
      PriorityMap=0x0f 017 0b1
      PriorityMap=0b+1
      PriorityMap=1 0b-0 2
      PriorityMap=0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
      PriorityMap=
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(0, highlights)
  }

  @Test
  fun testInvalidValues() {
    // sd-check at 0f0682b773 rejects 0b 1 and 0b +1 (the list splits on blanks, so the word is "0b") and 0b-1 (ERANGE).
    // language="unit file (systemd)"
    val file = """
      [EnhancedTransmissionSelection]
      PriorityMap=256
      PriorityMap=-1
      PriorityMap=1,2
      PriorityMap=a b
      PriorityMap=0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 0
      PriorityMap=0b 1
      PriorityMap=0b +1
      PriorityMap=0b-1
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting()

    assertSize(8, highlights)
  }

}
