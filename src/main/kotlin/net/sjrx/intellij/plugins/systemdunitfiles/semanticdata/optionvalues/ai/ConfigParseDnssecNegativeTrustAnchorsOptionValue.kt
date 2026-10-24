package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.DNS_NAME_WORD
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.wordList

/*
 * [Network] DNSSECNegativeTrustAnchors= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#DNSSECNegativeTrustAnchors=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dns.c#L270-L319    config_parse_dnssec_negative_trust_anchors (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188       extract_first_word (whitespace, flags 0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/dns-domain.h#L26-L36         dns_name_is_valid
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/dns-domain.c#L24-L150        dns_label_unescape
 */

/**
 * Validator for `[Network] DNSSECNegativeTrustAnchors=`: a whitespace-separated list of DNS names, each
 * checked by dns_name_is_valid() (non-LDH: any printable label characters, single dots, optional
 * trailing dot, `.` for the root). See [DNS_NAME_WORD].
 */
class ConfigParseDnssecNegativeTrustAnchorsOptionValue : SimpleGrammarOptionValues(
    "config_parse_dnssec_negative_trust_anchors",
    SequenceCombinator(wordList(DNS_NAME_WORD), EOF())
)
