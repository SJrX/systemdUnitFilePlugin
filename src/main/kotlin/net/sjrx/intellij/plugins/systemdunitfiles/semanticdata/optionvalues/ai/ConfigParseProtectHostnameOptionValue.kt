package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for Service.ProtectHostname (also Socket/Mount/Swap.ProtectHostname).
 * C Function: config_parse_protect_hostname(0)
 *
 * Accepts either:
 *   - a boolean (yes/no/y/n/true/false/t/f/1/0/on/off) or "private"
 *   - "<keyword>:<hostname>" where <keyword> is a "true" boolean (yes/y/true/t/1/on)
 *     or "private", and <hostname> matches hostname_is_valid (LDH chars; labels
 *     joined by '.' with no leading/trailing hyphens).
 *
 * The C code rejects "no:<hostname>" (and other false synonyms with a hostname).
 */
class ConfigParseProtectHostnameOptionValue : SimpleGrammarOptionValues(
    "config_parse_protect_hostname",
    SequenceCombinator(
        AlternativeCombinator(
            // Form: <true-keyword-or-private>:<hostname>
            SequenceCombinator(
                BOOLEAN_TRUE_OR_PRIVATE,
                COLON_LITERAL,
                HOSTNAME
            ),
            // Form: standalone keyword (any boolean or "private")
            BOOLEAN_OR_PRIVATE
        ),
        EOF()
    )
) {
    companion object {
        private val COLON_LITERAL = LiteralChoiceTerminal(":")

        // Boolean true synonyms plus "private" (used when a hostname is supplied).
        private val BOOLEAN_TRUE_OR_PRIVATE = FlexibleLiteralChoiceTerminal(
            "yes", "y", "true", "t", "on", "1", "private"
        )

        // Any boolean (true or false synonyms) plus "private".
        private val BOOLEAN_OR_PRIVATE = FlexibleLiteralChoiceTerminal(
            "1", "yes", "y", "true", "t", "on",
            "0", "no", "n", "false", "f", "off",
            "private"
        )

        // Hostname: one or more LDH labels separated by '.'. Each label starts and
        // ends with [A-Za-z0-9] and may contain hyphens internally.
        private val HOSTNAME = RegexTerminal(
            "[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?(\\.[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?)*",
            "[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?(\\.[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?)*"
        )
    }
}
