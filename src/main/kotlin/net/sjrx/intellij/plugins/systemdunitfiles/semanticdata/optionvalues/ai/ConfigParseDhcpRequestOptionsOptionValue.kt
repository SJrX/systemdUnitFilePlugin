package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.wordList

/*
 * [DHCPv4] RequestOptions= (AF_INET), [DHCPv6] RequestOptions= (AF_INET6) (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#RequestOptions=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L1110-L1174  config_parse_dhcp_request_options (AF_INET / AF_INET6)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188              extract_first_word (whitespace, flags 0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478               safe_atou_full (safe_atou32, base 0)
 */

/**
 * Validator for `RequestOptions=` in both [DHCPv4] and [DHCPv6] (config_parse_dhcp_request_options;
 * the two ltypes only pick which set is filled): a whitespace-separated list of option codes, each read
 * by safe_atou32 and required to be 1…254 — for DHCPv6 too, although its option space is 16-bit; the C
 * check is `i < 1 || i >= UINT8_MAX` for both families.
 *
 * Each code is a word from a whitespace split, so whitespace never reaches safe_atou32
 * (acceptsWhitespace = false): `RequestOptions=0b 1` is the words `0b` and `1`, and `0b` is refused
 * ("DHCP request option is invalid", checked with systemd's parser at 0f0682b773).
 */
class ConfigParseDhcpRequestOptionsOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_request_options",
    SequenceCombinator(
        wordList(
            SafeAtoTerminal(
                signed = false,
                base = 0,
                flags = emptySet(),
                minInclusive = 1L,
                maxInclusive = 254L,
                acceptsWhitespace = false,
                acceptsMinus = true,
            ),
        ),
        EOF(),
    )
)
