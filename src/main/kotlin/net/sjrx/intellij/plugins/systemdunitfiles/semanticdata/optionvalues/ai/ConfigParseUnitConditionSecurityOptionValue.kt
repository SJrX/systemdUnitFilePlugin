package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

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
