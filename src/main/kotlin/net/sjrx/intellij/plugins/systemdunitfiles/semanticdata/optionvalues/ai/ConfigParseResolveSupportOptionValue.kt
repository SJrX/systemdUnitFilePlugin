package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BOOLEAN_ANY_CASE
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Network] LLMNR= / MulticastDNS= (.network) and [Resolve] LLMNR= / MulticastDNS= (resolved.conf).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#LLMNR=
 *        https://www.freedesktop.org/software/systemd/man/latest/resolved.conf.html#LLMNR=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/resolve-util.c#L8         config_parse_resolve_support (0), DEFINE_CONFIG_PARSE_ENUM
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/resolve-util.c#L12-L17    resolve_support_table (DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/string-table.c#L32-L43     string_table_lookup_from_string_with_boolean
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L23-L46       parse_boolean
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-state-file-tests.network#L14-L15
 */

/**
 * Validator for `LLMNR=` and `MulticastDNS=` (config_parse_resolve_support): `resolve`, or any boolean.
 *
 * The lookup tries parse_boolean() first — case-insensitive, all twelve spellings — and only then the
 * case-sensitive table (`no`, `yes`, `resolve`), so `Yes` is fine but `Resolve` is not.
 */
class ConfigParseResolveSupportOptionValue : SimpleGrammarOptionValues(
    "config_parse_resolve_support",
    AlternativeCombinator(
        SequenceCombinator(FlexibleLiteralChoiceTerminal("resolve"), EOF()),
        SequenceCombinator(BOOLEAN_ANY_CASE, EOF()),
    )
)
