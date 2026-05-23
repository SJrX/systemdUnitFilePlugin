package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for ExecSearchPath= (and similar colon-separated path lists).
 *
 * C function: config_parse_colon_separated_paths in src/core/load-fragment.c. Tokenizes with
 * extract_first_word using ":" as the separator (no coalescing), expands specifiers via
 * unit_path_printf, then requires each path to be absolute via path_simplify_and_warn with
 * PATH_CHECK_ABSOLUTE.
 *
 * Grammar matches one or more absolute paths (each starting with "/") separated by ":".
 * Per-path content excludes ":" since that's the separator. "%X" specifiers are allowed
 * inline since the C parser expands them before the absolute-path check.
 */
class ConfigParseColonSeparatedPathsOptionValue : SimpleGrammarOptionValues(
    "config_parse_colon_separated_paths",
    SequenceCombinator(
        RegexTerminal("/[^\\s:]*", "/[^\\s:]*"),
        ZeroOrMore(SequenceCombinator(
            LiteralChoiceTerminal(":"),
            RegexTerminal("/[^\\s:]*", "/[^\\s:]*")
        )),
        EOF()
    )
)
