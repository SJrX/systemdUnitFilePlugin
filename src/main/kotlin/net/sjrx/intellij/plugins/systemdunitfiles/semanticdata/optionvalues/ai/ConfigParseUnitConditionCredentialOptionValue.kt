package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.CONDITION_CREDENTIAL
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/*
 * [Unit] ConditionCredential= / AssertCredential=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionCredential=
 * parser https://github.com/systemd/systemd/blob/048970650c/src/core/load-fragment.c  config_parse_unit_condition_string (CONDITION_CREDENTIAL)
 * check  https://github.com/systemd/systemd/blob/048970650c/src/shared/condition.c    condition_test_credential
 *        https://github.com/systemd/systemd/blob/048970650c/src/shared/creds-util.c   credential_name_valid
 */

/**
 * Validator for `[Unit] ConditionCredential=` / `AssertCredential=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_CREDENTIAL. condition_test_credential
 * (src/shared/condition.c) rejects the parameter outright unless credential_name_valid() accepts it, so
 * that is the whole shape rule. credential_name_valid() (src/shared/creds-util.c) is
 * filename_is_valid() && fdname_is_valid():
 *
 *  - filename_is_valid(): non-empty, not "." or "..", no "/", at most NAME_MAX (255) bytes.
 *  - fdname_is_valid():   every byte printable ASCII (>= 0x20 and < 0x7F), no ":".
 *
 * Together: one to 255 printable-ASCII characters, none of them `/` or `:`, and not the reserved
 * names `.` or `..`. Space (0x20) is a printable character, so it is allowed inside a name.
 */
class ConfigParseUnitConditionCredentialOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(CONDITION_CREDENTIAL)
)
