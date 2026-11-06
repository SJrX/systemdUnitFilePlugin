package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import net.sjrx.intellij.plugins.systemdunitfiles.settings.PreferredMacFormat

/*
 * 6-byte MAC address parsing and re-formatting. The sibling of Ipv6.kt: parse the address to its raw
 * bytes, then print those bytes in the requested style.
 *
 * Accepted spellings (systemd parse_ether_addr, as matched by the MAC_ADDRESS grammar in Combinators.kt):
 *   colon:   1:2:3:a:B:cc          six fields of 1-2 hex digits
 *   hyphen:  01-02-03-0A-0B-CC     six fields of 1-2 hex digits
 *   dot:     102.30a.bcc           three fields of 1-4 hex digits, each one 16-bit word (2 bytes)
 * Short fields are zero-padded on the left, so the colon example is 01:02:03:0a:0b:cc.
 */

private val COLON_MAC = Regex("([0-9a-fA-F]{1,2}:){5}[0-9a-fA-F]{1,2}")
private val HYPHEN_MAC = Regex("([0-9a-fA-F]{1,2}-){5}[0-9a-fA-F]{1,2}")
private val DOT_MAC = Regex("([0-9a-fA-F]{1,4}\\.){2}[0-9a-fA-F]{1,4}")

/**
 * The six bytes of a MAC address in any accepted spelling, or `null` if [text] isn't one (wrong field
 * count or width, non-hex digits, mixed separators).
 */
fun macAddressBytes(text: String): IntArray? {
  val bytes = when {
    COLON_MAC.matches(text) -> text.split(':').map { it.toInt(16) }
    HYPHEN_MAC.matches(text) -> text.split('-').map { it.toInt(16) }
    DOT_MAC.matches(text) -> text.split('.').flatMap {
      val word = it.toInt(16)
      listOf(word shr 8, word and 0xFF)
    }
    else -> return null
  }
  return bytes.toIntArray()
}

/**
 * Returns [address] rewritten in [format], or `null` when there's nothing to suggest:
 * [PreferredMacFormat.LEAVE_ALONE], or [address] isn't a parseable 6-byte MAC.
 *
 * The result is compared with the original text, and the inspection fires only when they differ, so
 * an address already in [format] comes back unchanged (same case, same zero padding).
 */
fun formatMac(address: String, format: PreferredMacFormat): String? {
  if (format == PreferredMacFormat.LEAVE_ALONE) return null
  val bytes = macAddressBytes(address) ?: return null
  return when (format) {
    PreferredMacFormat.LEAVE_ALONE -> null
    PreferredMacFormat.COLON_LOWER -> bytes.joinToString(":") { "%02x".format(it) }
    PreferredMacFormat.COLON_UPPER -> bytes.joinToString(":") { "%02X".format(it) }
    PreferredMacFormat.HYPHEN_UPPER -> bytes.joinToString("-") { "%02X".format(it) }
    PreferredMacFormat.DOT_LOWER -> bytes.toList().chunked(2).joinToString(".") { (high, low) -> "%02x%02x".format(high, low) }
  }
}
