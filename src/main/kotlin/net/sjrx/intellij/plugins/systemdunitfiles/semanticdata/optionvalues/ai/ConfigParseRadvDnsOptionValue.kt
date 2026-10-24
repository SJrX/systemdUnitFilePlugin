package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV6_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.wordList

/*
 * [IPv6SendRA] DNS= (and the old [IPv6PrefixDelegation] DNS=) (.network).
 *
 * Note: the plugin reports the deprecated [IPv6PrefixDelegation] section as not allowed in .network files, so this
 * validator never actually runs for keys there; they are listed only because the gperf shares the parser.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#DNS=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-radv.c#L1285-L1347   config_parse_radv_dns (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188         extract_first_word (whitespace, flags 0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L517-L529        in_addr_from_string (AF_INET6, inet_pton)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-ipv6-prefix.network#L10
 * linux  https://man7.org/linux/man-pages/man3/inet_pton.3.html
 */

/**
 * Validator for `[IPv6SendRA] DNS=` (config_parse_radv_dns): a whitespace-separated list of IPv6
 * addresses (inet_pton AF_INET6 — no brackets, ports or `%scope`) or the special word `_link_local`.
 *
 * systemd also drops the null address (`::`) with a warning; spellings of it are not singled out here
 * (lenient).
 */
class ConfigParseRadvDnsOptionValue : SimpleGrammarOptionValues(
    "config_parse_radv_dns",
    SequenceCombinator(wordList(AlternativeCombinator(LiteralChoiceTerminal("_link_local"), IPV6_ADDR)), EOF())
)
