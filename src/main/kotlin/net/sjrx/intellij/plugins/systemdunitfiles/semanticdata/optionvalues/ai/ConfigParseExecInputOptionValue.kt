package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for StandardInput=.
 *
 * C function: config_parse_exec_input,
 * https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L1136-L1206. Accepts:
 *   - "fd:NAME" — NAME may be empty, otherwise fdname_is_valid(): printable ASCII except ':', at most 255
 *     characters (https://github.com/systemd/systemd/blob/0f0682b773/src/basic/fd-util.c#L554-L579). Spaces
 *     are allowed.
 *   - "file:PATH" — PATH goes through path_simplify_and_warn(PATH_CHECK_ABSOLUTE|PATH_CHECK_FATAL): absolute
 *     (spaces allowed) with no ".." component after the leading ones path_simplify() drops
 *     (https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L34-L80). A path that
 *     starts with a specifier is only known once it is expanded, so it is accepted.
 *   - any name from exec_input_table, including the bare "fd" and "file"
 *     (https://github.com/systemd/systemd/blob/0f0682b773/src/core/execute.c#L3242-L3253).
 */
class ConfigParseExecInputOptionValue : SimpleGrammarOptionValues(
    "config_parse_exec_input",
    SequenceCombinator(
        // PreferValid: under the legacy engine the bare "file" choice would otherwise be matched out of an invalid
        // "file:rel" and mislocate the error.
        PreferValidAlternativeCombinator(
            SequenceCombinator(LiteralChoiceTerminal("fd:"), RegexTerminal(".*", "[\\x20-\\x39\\x3b-\\x7e]{0,255}")),
            SequenceCombinator(
                LiteralChoiceTerminal("file:"),
                RegexTerminal(".+", "%.*|/(?:\\.{0,2}(?:/|$))*(?!(?:.*/)?\\.\\.(?:/|$)).*")
            ),
            FlexibleLiteralChoiceTerminal("null", "tty", "tty-force", "tty-fail", "socket", "fd", "data", "file", "broker", "broker-log")
        ),
        EOF()
    )
)
