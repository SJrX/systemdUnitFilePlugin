package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/*
 * [Unit] ConditionGroup= / AssertGroup=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionGroup=
 * parser https://github.com/systemd/systemd/blob/048970650c/src/core/load-fragment.c  config_parse_unit_condition_string (CONDITION_GROUP)
 * check  https://github.com/systemd/systemd/blob/048970650c/src/shared/condition.c    condition_test_group
 */

/**
 * Validator for `[Unit] ConditionGroup=` / `AssertGroup=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_GROUP. condition_test_group
 * (src/shared/condition.c) accepts a numeric GID (parse_gid) or a group name (in_group / a `root`
 * match under PID 1). Both forms are a non-empty run of characters with no `:` or `/`, no control
 * characters, and no leading or trailing whitespace — the same lenient rule the plugin applies to
 * User=/Group= (config_parse_user_group_compat), with a numeric GID a special case of that shape.
 */
class ConfigParseUnitConditionGroupOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(
        RegexTerminal(".+", "[^\\s:/\\x00-\\x1F](?:[^:/\\x00-\\x1F]*[^\\s:/\\x00-\\x1F])?")
    )
)
