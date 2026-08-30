package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ENV_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FNMATCH_COMPARE_OPERATOR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/*
 * [Unit] ConditionOSRelease= / AssertOSRelease=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionOSRelease=
 * parser https://github.com/systemd/systemd/blob/048970650c/src/core/load-fragment.c  config_parse_unit_condition_string (CONDITION_OS_RELEASE)
 * check  https://github.com/systemd/systemd/blob/048970650c/src/shared/condition.c    condition_test_osrelease
 */

// A quoted expression (either quote style). The parser unquotes each whitespace-separated token before
// looking at it, so a quoted token may legally contain spaces; matched permissively rather than
// re-deriving the key/op/value split inside quotes, so a quoted value with spaces is never mis-flagged.
private val OSRELEASE_QUOTED = RegexTerminal("\"[^\"]*\"|'[^']*'", "\"[^\"]*\"|'[^']*'")

// One bare `KEY<op>VALUE` expression with no internal whitespace: an env-var-valid key, a comparison
// operator (with fnmatch), then a non-empty value. condition_test_osrelease rejects whitespace after the
// operator, so the three parts are contiguous.
private val OSRELEASE_BARE_EXPR = SequenceCombinator(ENV_NAME, FNMATCH_COMPARE_OPERATOR, RegexTerminal("\\S+", "\\S+"))

private val OSRELEASE_EXPR = AlternativeCombinator(OSRELEASE_QUOTED, OSRELEASE_BARE_EXPR)

/**
 * Validator for `[Unit] ConditionOSRelease=` / `AssertOSRelease=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_OS_RELEASE. condition_test_osrelease
 * (src/shared/condition.c) reads a whitespace-separated list of `KEY<op>VALUE` expressions. For each
 * token it requires: a key that env_name_is_valid() accepts, one of the comparison operators
 * (parse_compare_operator with fnmatch), no whitespace after the operator, and a non-empty value.
 *
 * Because the tokens are extracted with EXTRACT_UNQUOTE, a token may be quoted and then legally contain
 * spaces in its value; that form is accepted permissively rather than split, so a value with spaces is
 * never wrongly flagged. The bare form enforces the full key/op/value shape and so still catches the
 * real mistakes — a token with no operator, or a key that starts with a digit.
 */
class ConfigParseUnitConditionOsReleaseOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(
        SequenceCombinator(
            OSRELEASE_EXPR,
            ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), OSRELEASE_EXPR)),
        )
    )
)
