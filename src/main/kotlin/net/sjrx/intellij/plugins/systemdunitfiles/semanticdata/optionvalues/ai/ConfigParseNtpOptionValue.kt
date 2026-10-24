package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.DNS_NAME_WORD
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.wordList

/*
 * [Network] NTP= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#NTP=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-ntp.c#L48-L104       config_parse_ntp (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188         extract_first_word (whitespace, flags 0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/dns-domain.c#L1331-L1341       dns_name_is_valid_or_address
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/dns-domain.c#L24-L150          dns_label_unescape
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-state-file-tests.network#L12
 */

/**
 * Validator for `[Network] NTP=` (config_parse_ntp): a whitespace-separated list of NTP servers, each a
 * DNS name or an IP address (dns_name_is_valid_or_address).
 *
 * Every IP literal systemd accepts here is also a syntactically valid non-LDH DNS name (`192.168.5.1` is
 * four labels, `2001:db8::1` one label containing colons), so the grammar is simply a list of
 * [DNS_NAME_WORD]s; what it catches is empty labels (`pool..ntp.org`, `.org`), double trailing dots,
 * over-long labels and stray control characters. The MAX_NTP_SERVERS cap is not modelled.
 */
class ConfigParseNtpOptionValue : SimpleGrammarOptionValues(
    "config_parse_ntp",
    SequenceCombinator(wordList(DNS_NAME_WORD), EOF())
)
