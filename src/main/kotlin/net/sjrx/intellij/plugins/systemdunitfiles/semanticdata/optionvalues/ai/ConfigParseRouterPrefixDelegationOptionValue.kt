package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BOOLEAN_ANY_CASE
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Network] IPv6SendRA= and the deprecated [Network] IPv6PrefixDelegation= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#IPv6SendRA=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-radv.c#L1418-L1461   config_parse_router_prefix_delegation (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-radv.c#L1406-L1416   radv_prefix_delegation_table (DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L23-L48           parse_boolean (case-insensitive)
 */

/**
 * Validator for config_parse_router_prefix_delegation: `[Network] IPv6SendRA=` and its deprecated
 * predecessor `[Network] IPv6PrefixDelegation=`.
 *
 * The parser branches on the key name. For `IPv6SendRA=` it takes a plain parse_boolean() value. For
 * the backward-compatible `IPv6PrefixDelegation=` it uses radv_prefix_delegation_from_string(), a
 * string table with boolean fallback: any boolean, or `static` / `dhcpv6`.
 *
 * Both keys share one (parser, ltype) registration, so this grammar accepts the union — a boolean, or
 * `static` / `dhcpv6`. That is exact for `IPv6PrefixDelegation=` and lenient for `IPv6SendRA=`, where
 * `static` and `dhcpv6` are in fact rejected. Booleans are matched case-insensitively, as parse_boolean() does. The names come before the boolean so the classic matcher
 * tries them first.
 */
class ConfigParseRouterPrefixDelegationOptionValue : SimpleGrammarOptionValues(
    "config_parse_router_prefix_delegation",
    SequenceCombinator(
        AlternativeCombinator(
            FlexibleLiteralChoiceTerminal("static", "dhcpv6"),
            BOOLEAN_ANY_CASE,
        ),
        EOF()
    )
)
