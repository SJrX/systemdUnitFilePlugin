package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for RequiresMountsFor= / WantsMountsFor=.
 *
 * C function: config_parse_unit_mounts_for in src/core/load-fragment.c. Tokenizes rvalue with
 * extract_first_word (whitespace-separated, EXTRACT_UNQUOTE), expands unit specifiers via
 * unit_path_printf, then requires each path to be absolute via path_simplify_and_warn with
 * PATH_CHECK_ABSOLUTE.
 *
 * The grammar allows any non-whitespace path that starts with "/", and accepts "%X" specifiers
 * inline since the C parser expands them before the absolute-path check.
 */
class ConfigParseUnitMountsForOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_mounts_for",
    SequenceCombinator(
        RegexTerminal("/\\S*", "/\\S*"),
        ZeroOrMore(SequenceCombinator(
            WhitespaceTerminal(),
            RegexTerminal("/\\S*", "/\\S*")
        )),
        EOF()
    )
)
