package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ABSOLUTE_PATH_WITH_SPECIFIERS
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Combinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore

/*
 * Validators for unit-file settings that take absolute paths or unit names (#509).
 */

/**
 * Validator for the `[Path]` watch settings: `PathExists=`, `PathExistsGlob=`, `PathChanged=`,
 * `PathModified=` and `DirectoryNotEmpty=`.
 *
 * C function: config_parse_path_spec (src/core/load-fragment.c) — unit_path_printf() followed by
 * path_simplify_and_warn(..., PATH_CHECK_ABSOLUTE). One path per assignment; the value is never split
 * on whitespace, so two paths on one line is a single path containing a space.
 */
class ConfigParsePathSpecOptionValue : SimpleGrammarOptionValues(
    "config_parse_path_spec",
    SequenceCombinator(ABSOLUTE_PATH_WITH_SPECIFIERS, EOF())
)

/**
 * Validator for `[Socket] Symlinks=`.
 *
 * C function: config_parse_unit_path_strv_printf (src/core/load-fragment.c) — a whitespace-separated
 * list, each entry run through unit_path_printf() and then required to be absolute.
 */
class ConfigParseUnitPathStrvPrintfOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_path_strv_printf",
    SequenceCombinator(
        ABSOLUTE_PATH_WITH_SPECIFIERS,
        ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), ABSOLUTE_PATH_WITH_SPECIFIERS)),
        EOF()
    )
)

/**
 * Validator for `[Socket] Service=`.
 *
 * C function: config_parse_socket_service (src/core/load-fragment.c). After unit_name_printf() the
 * only shape check systemd makes is `endswith(p, ".service")` — everything else is deferred to
 * manager_load_unit(), which needs a running manager. So that suffix, and the character set a unit
 * name is allowed to draw from, is exactly what this grammar checks.
 */
class ConfigParseSocketServiceOptionValue : SimpleGrammarOptionValues(
    "config_parse_socket_service",
    SequenceCombinator(unitName("service"), EOF())
)

/**
 * Validator for `[Service] Sockets=`.
 *
 * C function: config_parse_service_sockets (src/core/load-fragment.c) — a whitespace-separated list
 * where each entry must end in `.socket`. Entries that don't are logged and skipped individually.
 */
class ConfigParseServiceSocketsOptionValue : SimpleGrammarOptionValues(
    "config_parse_service_sockets",
    SequenceCombinator(
        unitName("socket"),
        ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), unitName("socket"))),
        EOF()
    )
)

/**
 * A unit name with the given suffix. The character set is systemd's VALID_CHARS for unit names
 * (alphanumerics plus `:-_.\`) widened by `@`, which separates a template from its instance, and `%`,
 * because unit_name_printf() expands specifiers before the name is validated.
 */
private fun unitName(suffix: String): Combinator =
  RegexTerminal("""\S+""", """[A-Za-z0-9:_.\\@%-]+\.$suffix""")
