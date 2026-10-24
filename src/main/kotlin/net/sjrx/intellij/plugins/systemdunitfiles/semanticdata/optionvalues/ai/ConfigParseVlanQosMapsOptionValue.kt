package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.UINT32_LIMIT
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.optionallyQuoted
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.parseRange
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.safeAtou

/*
 * [VLAN] EgressQOSMaps= / IngressQOSMaps= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#EgressQOSMaps=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/vlan.c#L120-L180   config_parse_vlan_qos_maps (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188   extract_first_word (EXTRACT_CUNESCAPE|EXTRACT_UNQUOTE)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L305-L340   parse_range
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478   safe_atou_full (base 0)
 */

private val QOS_MAPPING = optionallyQuoted(parseRange(safeAtou(UINT32_LIMIT)))

/**
 * Validator for `[VLAN] EgressQOSMaps=` and `IngressQOSMaps=` (.netdev).
 *
 * C function: config_parse_vlan_qos_maps. A whitespace-separated list of words (each may be quoted);
 * every word goes to parse_range(), so it is `from-to` or a single number meaning `from == to`, each
 * side an unsigned int as safe_atou() reads it (decimal, `0x` hex, octal, `0b`/`0o`).
 *
 * The man page says each integer is in 1…4294967294 and that `from` must be >= `to`; the parser
 * enforces neither (it stores whatever parse_range returns), so this follows the C and accepts the full
 * unsigned range and any order. A malformed word is logged and skipped by systemd.
 */
class ConfigParseVlanQosMapsOptionValue : SimpleGrammarOptionValues(
    "config_parse_vlan_qos_maps",
    SequenceCombinator(QOS_MAPPING, ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), QOS_MAPPING)), EOF())
)
