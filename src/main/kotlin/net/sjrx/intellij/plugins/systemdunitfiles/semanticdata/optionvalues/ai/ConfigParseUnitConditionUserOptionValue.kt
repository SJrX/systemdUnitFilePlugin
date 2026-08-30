package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/*
 * [Unit] ConditionUser= / AssertUser=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionUser=
 * parser https://github.com/systemd/systemd/blob/048970650c/src/core/load-fragment.c  config_parse_unit_condition_string (CONDITION_USER)
 * check  https://github.com/systemd/systemd/blob/048970650c/src/shared/condition.c    condition_test_user
 */

/**
 * Validator for `[Unit] ConditionUser=` / `AssertUser=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_USER. condition_test_user
 * (src/shared/condition.c) accepts the literal `@system`, a numeric UID (parse_uid), or a user name
 * (matched against the running user's name / looked up with get_user_creds). `root` and the configured
 * nobody name are just user names handled by that last path.
 *
 * All of those forms are a non-empty run of characters with no `:` or `/`, no control characters, and
 * no leading or trailing whitespace — the same lenient rule the plugin already applies to User=/Group=
 * (config_parse_user_group_compat). A numeric UID and `@system` are special cases of that shape, so one
 * terminal covers every accepted value without an alternation that could shadow a name beginning
 * `@system…`.
 */
class ConfigParseUnitConditionUserOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(
        RegexTerminal(".+", "[^\\s:/\\x00-\\x1F](?:[^:/\\x00-\\x1F]*[^\\s:/\\x00-\\x1F])?")
    )
)
