package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NETWORK_DHCP_MISC_COALESCED_COLONS
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Combinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NetworkDhcpMiscSafeAtoNumberTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore

/*
 * DUIDType= — [DHCPv6] DUIDType= (.network, config_parse_duid_type), [DHCPv4] DUIDType= and the old
 * [DHCP] DUIDType= (.network, config_parse_network_duid_type), and [DHCPv4]/[DHCPv6] DUIDType= in
 * networkd.conf (config_parse_duid_type).
 *
 * Note: the plugin reports the deprecated [DHCP] section as not allowed in .network files, so this
 * validator never actually runs for keys there; they are listed only because the gperf shares the parser.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/networkd.conf.html#DUIDType=
 *        https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#DUIDType=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L1195-L1272  config_parse_duid_type (0)
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L1298-L1319  config_parse_network_duid_type (0) → config_parse_duid_type
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L1187-L1193  duid_type_table
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188              extract_first_word (":" separator, coalescing)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L599-L613               safe_atou16_full (base 0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1083-L1145                parse_timestamp
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/network/test-networkd-conf.c#L25-L37
 *        https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-dhcp-client-ipv6-only-custom-client-identifier.network#L14
 */

private val DUID_TYPE_NAME = FlexibleLiteralChoiceTerminal("link-layer-time", "vendor", "link-layer", "uuid")

// parse_timestamp() takes the whole remainder (it may itself contain spaces and colons, e.g.
// "2000-01-01 00:00:00 UTC", "@1700000000", "yesterday"); its grammar is large and is not modelled.
private val DUID_TIMESTAMP = RegexTerminal(""".+""", """.+""")

private fun duidType(): Combinator = SequenceCombinator(
    ZeroOrMore(COLON),
    AlternativeCombinator(
        // Only DUID-LLT (type 1) may carry a time; spelled either by name or as the number 1.
        SequenceCombinator(FlexibleLiteralChoiceTerminal("link-layer-time"), NETWORK_DHCP_MISC_COALESCED_COLONS, DUID_TIMESTAMP, EOF()),
        SequenceCombinator(NetworkDhcpMiscSafeAtoNumberTerminal(1, 2), NETWORK_DHCP_MISC_COALESCED_COLONS, DUID_TIMESTAMP, EOF()),
        SequenceCombinator(DUID_TYPE_NAME, ZeroOrMore(COLON), EOF()),
        SequenceCombinator(NetworkDhcpMiscSafeAtoNumberTerminal(0, 65_536), ZeroOrMore(COLON), EOF()),
    ),
)

/**
 * Validator for `DUIDType=`, registered for both config_parse_duid_type and config_parse_network_duid_type
 * (the latter only forwards to the former, twice).
 *
 * The value is `TYPE[:TIME]`. TYPE is one of `link-layer-time`, `vendor`, `link-layer`, `uuid`
 * (case-sensitive) or any number 0…65535 read by safe_atou16 (so `0x42`, as systemd's own test data
 * writes, is legal). A `:TIME` suffix — anything parse_timestamp() accepts — is allowed only when the type
 * is DUID-LLT, i.e. `link-layer-time` or the number 1; `vendor:2000-01-01` is refused, as
 * test-networkd-conf.c checks. The separator coalesces, so leading or trailing colons are harmless.
 *
 * The timestamp text itself is accepted as-is (lenient).
 */
class ConfigParseDuidTypeOptionValue(validatorName: String = "config_parse_duid_type") : SimpleGrammarOptionValues(
    validatorName,
    duidType(),
)
