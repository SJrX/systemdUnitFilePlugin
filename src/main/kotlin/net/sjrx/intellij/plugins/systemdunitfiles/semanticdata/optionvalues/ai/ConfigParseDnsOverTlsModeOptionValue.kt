package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BOOLEAN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.PreferValidAlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Network] DNSOverTLS= (.network) and [Resolve] DNSOverTLS= (resolved.conf).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#DNSOverTLS=
 *        https://www.freedesktop.org/software/systemd/man/latest/resolved.conf.html#DNSOverTLS=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/resolve-util.c#L10        config_parse_dns_over_tls_mode (0), DEFINE_CONFIG_PARSE_ENUM
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/resolve-util.c#L26-L31    dns_over_tls_mode_table (DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/string-table.c#L32-L43     string_table_lookup_from_string_with_boolean
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L23-L46       parse_boolean
 */

/**
 * Validator for `DNSOverTLS=` (config_parse_dns_over_tls_mode): `opportunistic`, or any boolean
 * (parse_boolean first, case-insensitively; then the case-sensitive table `no`/`opportunistic`/`yes`).
 */
class ConfigParseDnsOverTlsModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_dns_over_tls_mode",
    SequenceCombinator(
        PreferValidAlternativeCombinator(
            FlexibleLiteralChoiceTerminal("opportunistic"),
            BOOLEAN,
        ),
        EOF(),
    )
)
