package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for ManagedOOMRulesets= and friends.
 *
 * C function: config_parse_managed_oom_rules in src/core/load-fragment.c. After an
 * unsupported-unit-type check it tokenizes rvalue with extract_first_word and accepts each
 * token that passes string_is_safe(STRING_FILENAME) — i.e. filename-safe characters, not
 * "." / "..", no slashes, no whitespace, no shell metacharacters. The rules themselves are
 * loaded from .oomrule files at runtime so the grammar only validates the per-token shape.
 */
class ConfigParseManagedOomRulesOptionValue : SimpleGrammarOptionValues(
    "config_parse_managed_oom_rules",
    SequenceCombinator(
        RULE_NAME,
        ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), RULE_NAME)),
        EOF()
    )
) {
    companion object {
        // STRING_FILENAME forbids slashes, NUL, whitespace, and shell metacharacters. The
        // regex below is a conservative ASCII printable set excluding those plus the "." /
        // ".." filename reservations (rejected via the negative lookahead).
        private val RULE_NAME = RegexTerminal(
            "(?!\\.{1,2}\\Z)[A-Za-z0-9._-][A-Za-z0-9._+-]*",
            "(?!\\.{1,2}\\Z)[A-Za-z0-9._-][A-Za-z0-9._+-]*"
        )
    }
}
