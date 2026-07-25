package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/*
 * [Unit] ConditionSecurity= / AssertSecurity=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionSecurity=
 * parser https://github.com/systemd/systemd/blob/a8e93919c3/src/core/load-fragment.c   config_parse_unit_condition_string (CONDITION_SECURITY)
 * check  https://github.com/systemd/systemd/blob/a8e93919c3/src/shared/condition.c     condition_test_security
 *
 * There is no lookup table for this one: condition_test_security is a chain of streq() calls against
 * the whole parameter, which is why it takes exactly one technology and never a list.
 */

/**
 * Validator for `[Unit] ConditionSecurity=` / `AssertSecurity=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_SECURITY; the parameter is
 * compared with streq() against a closed list in condition_test_security (src/shared/condition.c).
 * A single technology only — condition_test_security never splits the parameter, so `apparmor selinux`
 * is not two values, it is one unrecognised one.
 */
class ConfigParseUnitConditionSecurityOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(SECURITY)
) {
    companion object {
        private val SECURITY = FlexibleLiteralChoiceTerminal(
            "selinux",
            "smack",
            "apparmor",
            "audit",
            "ima",
            "tomoyo",
            "uefi-secureboot",
            "tpm2",
            "cvm",
            "measured-uki",
            "measured-os",
        )
    }
}
