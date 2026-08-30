package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.CONDITION_VIRTUALIZATION
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/*
 * [Unit] ConditionVirtualization= / AssertVirtualization=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionVirtualization=
 * parser https://github.com/systemd/systemd/blob/048970650c/src/core/load-fragment.c   config_parse_unit_condition_string (CONDITION_VIRTUALIZATION)
 * check  https://github.com/systemd/systemd/blob/048970650c/src/shared/condition.c     condition_test_virtualization
 * values https://github.com/systemd/systemd/blob/048970650c/src/basic/virt.c           virtualization_table
 */

/**
 * Validator for `[Unit] ConditionVirtualization=` / `AssertVirtualization=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_VIRTUALIZATION; the parameter is
 * checked by condition_test_virtualization, which accepts `private-users`, a boolean, the categories
 * `vm`/`container`, or any virtualization_table id. The value grammar is shared with the
 * `.network`/`.netdev`/`.link` `[Match]` Virtualization= key — see [CONDITION_VIRTUALIZATION].
 */
class ConfigParseUnitConditionVirtualizationOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(CONDITION_VIRTUALIZATION)
)
