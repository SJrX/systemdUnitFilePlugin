package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.ValueWarning
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.ValueWarnings
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IP_ADDR_AND_SAFE_PREFIX
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ParseOutcome
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.canonicalizeIpv6
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.validate
import java.math.BigInteger
import java.net.Inet6Address
import java.net.InetAddress

/*
 * [WireGuardPeer] AllowedIPs= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#AllowedIPs=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/wireguard.c#L673-L749   config_parse_wireguard_allowed_ips (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188   extract_first_word ("," WHITESPACE, no flags)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L845-L897   in_addr_prefix_from_string_auto_full
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L779-L797   in_addr_parse_prefixlen (safe_atou8)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-wireguard.netdev#L15   AllowedIPs=fd31:bf08:57cb::/48,192.168.26.3/24
 */

// One extract_first_word() separator run; "," and whitespace both separate and runs coalesce.
private val ALLOWED_IPS_SEPARATOR = RegexTerminal("[\\s,]+", "[\\s,]+")

/**
 * Validator for `[WireGuardPeer] AllowedIPs=` (.netdev).
 *
 * C function: config_parse_wireguard_allowed_ips. A list of IPv4/IPv6 addresses with optional prefix
 * lengths, separated by commas and/or whitespace. Each entry goes to in_addr_prefix_from_string_auto: a
 * missing length means the full width, `/0` is allowed, and the length is a safe_atou8 number, so `/0x18`
 * works (see [IP_ADDR_AND_SAFE_PREFIX]). Separators coalesce, and leading or trailing ones are harmless.
 * An address that is not properly masked (`192.168.26.3/24`) is accepted and masked with a warning.
 * An invalid entry is logged and skipped by systemd; an empty assignment clears the list.
 */
class ConfigParseWireguardAllowedIpsOptionValue : SimpleGrammarOptionValues(
    "config_parse_wireguard_allowed_ips",
    SequenceCombinator(
        ZeroOrOne(ALLOWED_IPS_SEPARATOR),
        IP_ADDR_AND_SAFE_PREFIX,
        ZeroOrMore(SequenceCombinator(ALLOWED_IPS_SEPARATOR, IP_ADDR_AND_SAFE_PREFIX)),
        ZeroOrOne(ALLOWED_IPS_SEPARATOR),
        EOF()
    )
), ValueWarnings {

    /**
     * An entry whose address has bits set beyond the prefix length is masked with the warning "Specified address '…' is
     * not properly masked, assuming '…'".
     * https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/wireguard.c#L726-L732
     */
    override fun valueWarnings(key: String, value: String): List<ValueWarning> {
        if (combinator.validate(value) != ParseOutcome.Valid) return emptyList()
        return ALLOWED_IPS_ENTRY.findAll(value).mapNotNull { entry ->
            val masked = maskedPrefix(entry.value) ?: return@mapNotNull null
            ValueWarning(
                entry.range.first, entry.range.last + 1,
                "$key entry ${entry.value} has host bits set beyond the prefix length; systemd uses $masked instead. " +
                    "Write $masked."
            )
        }.toList()
    }
}

private val ALLOWED_IPS_ENTRY = Regex("[^\\s,]+")

/** `ADDRESS/N` with the host bits cleared, or null if [entry] has no prefix length, is already masked, or can't be read. */
private fun maskedPrefix(entry: String): String? {
    val slash = entry.indexOf('/')
    if (slash < 0) return null
    val address = entry.substring(0, slash)
    val prefixLength = parseSafeAtou8(entry.substring(slash + 1)) ?: return null
    val bytes = if (address.contains(':')) {
        (InetAddress.getByName(address) as? Inet6Address)?.address ?: return null
    } else {
        val octets = address.split('.').map { it.toIntOrNull() ?: return null }
        if (octets.size != 4 || octets.any { it !in 0..255 }) return null
        octets.map { it.toByte() }.toByteArray()
    }
    val width = bytes.size * 8
    if (prefixLength > width) return null
    val value = BigInteger(1, bytes)
    val hostBits = BigInteger.ONE.shiftLeft(width - prefixLength).subtract(BigInteger.ONE)
    val masked = value.andNot(hostBits)
    if (masked == value) return null
    // BigInteger.toByteArray() drops leading zero bytes (and may add a sign byte); pad back to the address size.
    val raw = ByteArray(bytes.size) { i -> masked.shiftRight(8 * (bytes.size - 1 - i)).toInt().toByte() }
    val text = if (bytes.size == 4) {
        raw.joinToString(".") { (it.toInt() and 0xff).toString() }
    } else {
        val groups = (0 until 8).joinToString(":") { i ->
            Integer.toHexString(((raw[2 * i].toInt() and 0xff) shl 8) or (raw[2 * i + 1].toInt() and 0xff))
        }
        canonicalizeIpv6(groups) ?: groups
    }
    return "$text/$prefixLength"
}

/** safe_atou8() with base 0 as in_addr_parse_prefixlen() calls it: decimal, 0x hex or leading-0 octal. */
private fun parseSafeAtou8(text: String): Int? {
    val digits = text.removePrefix("+")
    val parsed = when {
        digits.startsWith("-") -> if (digits.drop(1).all { it == '0' } && digits.length > 1) 0 else null
        digits.startsWith("0x") || digits.startsWith("0X") -> digits.drop(2).toIntOrNull(16)
        digits.length > 1 && digits.startsWith("0") -> digits.drop(1).toIntOrNull(8)
        else -> digits.toIntOrNull()
    }
    return parsed?.takeIf { it in 0..255 }
}
