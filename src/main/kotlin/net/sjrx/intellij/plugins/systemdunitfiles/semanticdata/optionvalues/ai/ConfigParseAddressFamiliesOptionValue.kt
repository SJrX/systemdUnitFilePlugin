package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for RestrictAddressFamilies=.
 *
 * C function: config_parse_address_families in src/core/load-fragment.c → parse_address_families
 * in src/shared/parse-helpers.c:90. Accepts:
 *   - "none" (clears the set, sets allowlist)
 *   - optional leading "~" (invert / denylist mode), followed by a whitespace-separated list
 *     of address family names from af_from_name (AF_UNIX, AF_INET, AF_INET6, AF_NETLINK,
 *     AF_PACKET, …)
 *
 * The grammar matches the "AF_" prefix loosely (any uppercase/digit/underscore tail); unknown
 * names slip past the grammar but fail at runtime. This is the same tradeoff as syscall_errno.
 */
class ConfigParseAddressFamiliesOptionValue : SimpleGrammarOptionValues(
    "config_parse_address_families",
    SequenceCombinator(
        AlternativeCombinator(
            LiteralChoiceTerminal("none"),
            SequenceCombinator(
                ZeroOrOne(LiteralChoiceTerminal("~")),
                RegexTerminal("AF_[A-Z0-9_]+", "AF_[A-Z0-9_]+"),
                ZeroOrMore(SequenceCombinator(
                    WhitespaceTerminal(),
                    RegexTerminal("AF_[A-Z0-9_]+", "AF_[A-Z0-9_]+")
                ))
            )
        ),
        EOF()
    )
)
