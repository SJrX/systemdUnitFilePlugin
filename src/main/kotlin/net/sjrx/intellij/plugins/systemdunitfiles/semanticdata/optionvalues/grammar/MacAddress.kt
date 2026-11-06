package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import net.sjrx.intellij.plugins.systemdunitfiles.settings.PreferredMacFormat

/*
 * MAC address re-formatting for MacAddressFormatInspection. The sibling of Ipv6.kt: parse the address
 * to its raw bytes, then print those bytes in the requested style.
 *
 * Input is whatever the MAC_ADDRESS grammar matched (Combinators.kt), i.e. one of:
 *   colon:   1:2:3:a:B:cc          six fields of 1-2 hex digits
 *   hyphen:  01-02-03-0A-0B-CC     six fields of 1-2 hex digits
 *   dot:     102.30a.bcc           three fields of 1-4 hex digits, each one 16-bit word (2 bytes)
 * Short fields are zero-padded on the left, so the colon example is 01:02:03:0a:0b:cc.
 */

/**
 * Returns [address] rewritten in [format], or `null` when there's nothing to suggest:
 * [PreferredMacFormat.LEAVE_ALONE], or [address] isn't a parseable 6-byte MAC.
 *
 * The result is compared with the original text, and the inspection fires only when they differ, so
 * an address already in [format] must come back unchanged (same case, same zero padding).
 */
fun formatMac(address: String, format: PreferredMacFormat): String? {

    var sixBlocks: List<Int>
    if (address.matches("([a-fA-F0-9]{1,2}:){5}[a-fA-F0-9]{1,2}".toRegex())) {
        sixBlocks = address.split(":").map { it -> it.toInt(16) }
    } else if (address.matches("([a-fA-F0-9]{1,2}-){5}[a-fA-F0-9]{1,2}".toRegex())) {
        sixBlocks = address.split("-").map { it -> it.toInt(16) }
    } else if (address.matches("([a-fA-F0-9]{1,4}\\.){2}[a-fA-F0-9]{1,4}".toRegex())) {
        sixBlocks = address.split(".").map { it.toInt(16) }.flatMap { listOf(it /256, it % 256) }
    } else {
        return null
    }

    if (sixBlocks.size != 6) {
        throw IllegalStateException("Uh this shouldn't be possible")
    }


    return when (format) {
        PreferredMacFormat.LEAVE_ALONE ->  null
        PreferredMacFormat.COLON_LOWER ->  sixBlocks.map { "%02x".format(it) }.joinToString(":")
        PreferredMacFormat.COLON_UPPER -> sixBlocks.map { "%02X".format(it) }.joinToString(":")
        PreferredMacFormat.HYPHEN_UPPER -> sixBlocks.map { "%02X".format(it) }.joinToString("-")
        PreferredMacFormat.DOT_LOWER -> sixBlocks.chunked(2).joinToString(".") { "%02x%02x".format(it[0], it[1]) }
    }
}
