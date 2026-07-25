package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/**
 * Validator for `[Unit] ConditionControlGroupController=` / `AssertControlGroupController=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_CONTROL_GROUP_CONTROLLER;
 * condition_test_control_group_controller (src/shared/condition.c) special-cases the whole parameter
 * being `v1` or `v2`, and otherwise hands it to cg_mask_from_string (src/basic/cgroup-util.c), which
 * splits on whitespace and resolves each word through cgroup_controller_table.
 *
 * `v1`/`v2` are listed as ordinary list members rather than a separate whole-value alternative: they
 * are only meaningful alone, but cg_mask_from_string silently skips words it doesn't know, so mixing
 * them into a list is tolerated by systemd and flagging it would be a false positive.
 */
class ConfigParseUnitConditionControlGroupControllerOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(
        SequenceCombinator(
            CONTROLLER,
            ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), CONTROLLER))
        )
    )
) {
    companion object {
        private val CONTROLLER = FlexibleLiteralChoiceTerminal(
            "v1",
            "v2",
            "cpu",
            "cpuacct",
            "cpuset",
            "io",
            "blkio",
            "memory",
            "devices",
            "pids",
            "bpf-firewall",
            "bpf-devices",
            "bpf-foreign",
            "bpf-socket-bind",
            "bpf-restrict-network-interfaces",
            "bpf-bind-network-interface",
        )
    }
}
