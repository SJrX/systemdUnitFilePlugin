package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/*
 * [Bond] AdActorSystem= (.netdev)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#AdActorSystem=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/bond.c#L368-L405   config_parse_ad_actor_system
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/ether-addr-util.c#L196-L280   parse_hw_addr_full (via parse_ether_addr)
 */

/**
 * The six bytes of a [MAC_ADDRESS] spelling: colon- or hyphen-separated 1-byte fields, or three
 * dot-separated 2-byte fields (`0223.4567.89ab`, where a short field such as `123` is 0x0123).
 */
private fun macAddressBytes(text: String): IntArray? {
  val bytes = when {
    '.' in text -> text.split('.').flatMap { word ->
      val v = word.toIntOrNull(16) ?: return null
      listOf(v shr 8, v and 0xFF)
    }
    else -> text.split(':', '-').map { it.toIntOrNull(16) ?: return null }
  }
  return if (bytes.size == 6) bytes.toIntArray() else null
}

/**
 * Validator for [Bond] AdActorSystem= (.netdev).
 *
 * C Function: config_parse_ad_actor_system(0). parse_ether_addr() takes a 6-byte MAC in any of the three
 * spellings, and the parser then refuses the all-zero address and any multicast address (the low bit of
 * the first byte set): "Not an appropriate MAC address …, cannot be null or multicast". So
 * `01:23:45:67:89:ab`, `0123.4567.89ab` and `00:00:00:00:00:00` are refused and `02:23:45:67:89:ab` and
 * `0223.4567.89ab` are accepted (sd-check at 0f0682b773).
 */
class ConfigParseAdActorSystemOptionValue : SimpleGrammarOptionValues(
  "config_parse_ad_actor_system",
  SequenceCombinator(
    Checked(MAC_ADDRESS) { text ->
      macAddressBytes(text)?.let { bytes -> bytes.any { it != 0 } && (bytes[0] and 0x01) == 0 } ?: true
    },
    EOF(),
  ),
)
