package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.CONDITION_ARCHITECTURE
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/*
 * [Unit] ConditionArchitecture= / AssertArchitecture=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionArchitecture=
 * parser https://github.com/systemd/systemd/blob/048970650c/src/core/load-fragment.c   config_parse_unit_condition_string (CONDITION_ARCHITECTURE)
 * check  https://github.com/systemd/systemd/blob/048970650c/src/shared/condition.c     condition_test_architecture_parameter
 * values https://github.com/systemd/systemd/blob/048970650c/src/basic/architecture.c   architecture_table
 */

/**
 * Validator for `[Unit] ConditionArchitecture=` / `AssertArchitecture=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_ARCHITECTURE; the parameter is
 * checked by condition_test_architecture_parameter, which accepts the literal `native` or any name in
 * architecture_table. The value grammar is shared with the `.network`/`.netdev`/`.link` `[Match]`
 * Architecture= key — see [CONDITION_ARCHITECTURE].
 */
class ConfigParseUnitConditionArchitectureOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(CONDITION_ARCHITECTURE)
)
