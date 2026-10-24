package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.DNS_NAME_WORD
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.wordList

/*
 * [Network] Domains= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Domains=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dns.c#L134-L209    config_parse_domains (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188       extract_first_word (whitespace, flags 0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/dns-domain.c#L742-L749       dns_name_is_root
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/dns-domain.c#L24-L150        dns_label_unescape (via dns_name_normalize, no LDH)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/hostname-util.c#L145-L161     is_localhost
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-state-file-tests.network#L13
 *        https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/24-search-domain.network#L9
 */

// One Domains= word: an optional `~` (route-only), then the root domain `.`, the wildcard `*`, or a
// DNS name. `~` on its own is the root routing domain (dns_name_is_root("") is true). `*` needs no
// branch of its own: it is already a valid one-label non-LDH DNS name, and a separate `*` literal would
// strand the classic engine on a label such as `*foo`.
private val DOMAIN_WORD = SequenceCombinator(
    ZeroOrOne(LiteralChoiceTerminal("~")),
    ZeroOrOne(DNS_NAME_WORD),
)

/**
 * Validator for `[Network] Domains=` (config_parse_domains): a whitespace-separated list of search
 * domains, each optionally prefixed with `~` to make it a routing-only domain; `.`, `~.` and `*` select
 * the root/wildcard routing domain. Names are validated as non-LDH DNS names ([DNS_NAME_WORD]), so
 * underscores and most punctuation are fine but empty labels (`a..b`, `.a`) are not.
 *
 * systemd also ignores `localhost` and `*.localhost` here (is_localhost); that is not modelled (lenient).
 */
class ConfigParseDomainsOptionValue : SimpleGrammarOptionValues(
    "config_parse_domains",
    SequenceCombinator(wordList(DOMAIN_WORD), EOF())
)
