package net.sjrx.intellij.plugins.systemdunitfiles.inspections.ai

import net.sjrx.intellij.plugins.systemdunitfiles.AbstractUnitFileTest
import net.sjrx.intellij.plugins.systemdunitfiles.inspections.InvalidValueInspection
import com.intellij.lang.annotation.HighlightSeverity
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
      PriorityMap=15
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

  @Test
  fun testBandsAboveFifteenNeverWork() {
    // The parser (safe_atou8) takes 16…255, but enhanced_transmission_selection_verify() drops the section when an
    // entry is not below the band count (src/network/tc/ets.c#L325-L330), which is at most 16 (#565).
    // language="unit file (systemd)"
    val file = """
      [EnhancedTransmissionSelection]
      PriorityMap=255
      PriorityMap=0 1 16
    """.trimIndent()

    setupFileInEditor("file.network", file)
    enableInspection(InvalidValueInspection::class.java)
    val highlights = myFixture.doHighlighting().filter { it.severity == HighlightSeverity.WARNING }

    assertSize(2, highlights)
    assertStringContains("PriorityMap: band '255' is out of range. Bands are numbered 0…15", highlights[0].description)
  }

}
