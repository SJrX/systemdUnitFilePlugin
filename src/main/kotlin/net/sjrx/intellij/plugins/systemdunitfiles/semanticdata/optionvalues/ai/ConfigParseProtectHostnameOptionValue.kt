package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for Service.ProtectHostname (also Socket/Mount/Swap.ProtectHostname).
 * C Function: config_parse_protect_hostname(0), src/core/load-fragment.c
 *
 * Accepts either:
 *   - a boolean or "private"
 *   - "<keyword>:<hostname>" where <keyword> is a "true" boolean or "private", and <hostname>
 *     matches hostname_is_valid (LDH chars; labels joined by '.' with no leading/trailing hyphens).
 *
 * The keyword goes through protect_hostname_from_string, a DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN over
 * protect_hostname_table (no, yes, private) in src/core/namespace.c: the boolean half ignores case
 * (parse_boolean), the names do not. The C code rejects "no:<hostname>" (and other false synonyms
 * with a hostname).
 */
class ConfigParseProtectHostnameOptionValue : SimpleGrammarOptionValues(
    "config_parse_protect_hostname",
    SequenceCombinator(
        AlternativeCombinator(
            // Form: <true-keyword-or-private>:<hostname>
            SequenceCombinator(
                PreferValidAlternativeCombinator(
                    FlexibleLiteralChoiceTerminal("private"),
                    BOOLEAN_TRUE,
                ),
                COLON_LITERAL,
                HOSTNAME
            ),
            // Form: standalone keyword (any boolean or "private")
            PreferValidAlternativeCombinator(
                FlexibleLiteralChoiceTerminal("private"),
                BOOLEAN,
            )
        ),
        EOF()
    )
) {
    companion object {
        private val COLON_LITERAL = LiteralChoiceTerminal(":")
        // Hostname: one or more LDH labels separated by '.'. Each label starts and
        // ends with [A-Za-z0-9] and may contain hyphens internally.
        private val HOSTNAME = RegexTerminal(
            "[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?(\\.[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?)*",
            "[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?(\\.[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?)*"
        )
    }
}
