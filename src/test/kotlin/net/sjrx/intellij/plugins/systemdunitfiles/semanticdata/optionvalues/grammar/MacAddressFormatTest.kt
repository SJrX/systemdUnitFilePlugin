package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import net.sjrx.intellij.plugins.systemdunitfiles.settings.PreferredMacFormat
import net.sjrx.intellij.plugins.systemdunitfiles.settings.PreferredMacFormat.COLON_LOWER
import net.sjrx.intellij.plugins.systemdunitfiles.settings.PreferredMacFormat.COLON_UPPER
import net.sjrx.intellij.plugins.systemdunitfiles.settings.PreferredMacFormat.DOT_LOWER
import net.sjrx.intellij.plugins.systemdunitfiles.settings.PreferredMacFormat.HYPHEN_UPPER
import net.sjrx.intellij.plugins.systemdunitfiles.settings.PreferredMacFormat.LEAVE_ALONE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Unit tests for [formatMac]. */
class MacAddressFormatTest {

  private val inputs = listOf(
    "00:1a:2b:3c:4d:5e",
    "00:1A:2B:3C:4D:5E",
    "00-1a-2b-3c-4d-5e",
    "00-1A-2B-3C-4D-5E",
    "001a.2b3c.4d5e",
    "001A.2B3C.4D5E",
  )

  @Test
  fun testEveryInputStyleConvertsToEveryFormat() {
    for (input in inputs) {
      assertEquals(input, "00:1a:2b:3c:4d:5e", formatMac(input, COLON_LOWER))
      assertEquals(input, "00:1A:2B:3C:4D:5E", formatMac(input, COLON_UPPER))
      assertEquals(input, "00-1A-2B-3C-4D-5E", formatMac(input, HYPHEN_UPPER))
      assertEquals(input, "001a.2b3c.4d5e", formatMac(input, DOT_LOWER))
    }
  }

  @Test
  fun testShortFieldsAreZeroPadded() {
    assertEquals("01:02:03:0a:0b:cc", formatMac("1:2:3:a:B:cc", COLON_LOWER))
    assertEquals("01-02-03-0A-0B-CC", formatMac("1-2-3-a-b-cc", HYPHEN_UPPER))
    // Dot fields are 16-bit words: "102" is 0x0102 -> bytes 01, 02.
    assertEquals("01:02:03:0a:0b:cc", formatMac("102.30a.bcc", COLON_LOWER))
    assertEquals("0102.030a.0bcc", formatMac("1:2:3:a:b:cc", DOT_LOWER))
  }

  @Test
  fun testAlreadyPreferredIsUnchanged() {
    for (format in PreferredMacFormat.entries - LEAVE_ALONE) {
      val preferred = formatMac("00:1a:2b:3c:4d:5e", format)!!
      assertEquals(format.name, preferred, formatMac(preferred, format))
    }
  }

  @Test
  fun testLeaveAloneReturnsNull() {
    assertNull(formatMac("00-1A-2B-3C-4D-5E", LEAVE_ALONE))
  }

  @Test
  fun testUnparseableReturnsNull() {
    assertNull(formatMac("", COLON_LOWER))
    assertNull(formatMac("00:11:22:33:44", COLON_LOWER))          // too few fields
    assertNull(formatMac("00:11:22:33:44:55:66", COLON_LOWER))    // too many
    assertNull(formatMac("00:11-22:33:44:55", COLON_LOWER))       // mixed separators
    assertNull(formatMac("0g:11:22:33:44:55", COLON_LOWER))       // not hex
    assertNull(formatMac("001:11:22:33:44:55", COLON_LOWER))      // byte field too long
  }
}
