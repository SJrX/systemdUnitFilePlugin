package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.PERMYRIAD_PERCENTAGE
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/*
 * [Unit] ConditionFraction= / AssertFraction=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionFraction=
 * parser https://github.com/systemd/systemd/blob/048970650c/src/core/load-fragment.c  config_parse_unit_condition_string (CONDITION_FRACTION)
 * check  https://github.com/systemd/systemd/blob/048970650c/src/shared/condition.c    condition_test_fraction
 */

/**
 * Validator for `[Unit] ConditionFraction=` / `AssertFraction=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_FRACTION. condition_test_fraction
 * (src/shared/condition.c) reads `[TAG ]PERCENT`: the mandatory trailing field is a percentage parsed by
 * parse_permyriad(), and an optional leading whitespace-separated field is a hash-salt tag (any word).
 * More than two fields is "trailing garbage".
 *
 * The tag is any single non-whitespace token; the percentage is a [PERMYRIAD_PERCENTAGE]. The
 * "tag + percent" alternative is listed first so it is tried before the bare percent (a lone token that
 * fails to be a percentage is rejected, as systemd rejects it).
 */
class ConfigParseUnitConditionFractionOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(
        AlternativeCombinator(
            SequenceCombinator(RegexTerminal("\\S+", "\\S+"), WhitespaceTerminal(), PERMYRIAD_PERCENTAGE),
            PERMYRIAD_PERCENTAGE,
        )
    )
)
