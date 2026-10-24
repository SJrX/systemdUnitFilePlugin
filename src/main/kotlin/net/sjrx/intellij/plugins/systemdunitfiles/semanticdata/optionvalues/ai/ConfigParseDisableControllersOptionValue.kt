package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/*
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L5787-L5820   config_parse_disable_controllers
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/cgroup-util.c#L1661-L1687   cg_mask_from_string
 * table  https://github.com/systemd/systemd/blob/0f0682b773/src/basic/cgroup-util.c#L1792-L1807   cgroup_controller_table
 */

/**
 * Validator for DisableControllers= ([Service], [Socket], [Mount], [Swap], [Slice], ...).
 * C Function: config_parse_disable_controllers(0)
 *
 * cg_mask_from_string() splits the value on whitespace and looks every word up case-sensitively in
 * cgroup_controller_table. A word that is not a controller is silently skipped, so `cpu invalid memory` is
 * fine. Only when no word at all names a controller (the mask stays 0) is the assignment refused
 * ("Invalid cgroup string"). The legacy cgroup v1 names `cpuacct`, `blkio` and `devices` are still in the
 * table and accepted.
 *
 * The grammar is therefore: any number of unknown words, then one known controller, then any words at all.
 * The unknown-word terminal refuses a known controller name (negative lookahead), so the classic engine,
 * which never backtracks, stops in front of the first known controller. It also takes the whitespace after
 * the word, so that the last word of an all-unknown value is left for the controller terminal to flag.
 */
class ConfigParseDisableControllersOptionValue : SimpleGrammarOptionValues(
    "config_parse_disable_controllers",
    SequenceCombinator(
        // An unknown word together with the whitespace after it, as one token: the classic engine reports a
        // problem past the end of the value when a repetition matches a word but not the whitespace after it.
        ZeroOrMore(
            RegexTerminal(
                "(?!(?:$CONTROLLER_NAMES)(?!\\S))\\S+\\s+",
                "(?!(?:$CONTROLLER_NAMES)(?!\\S))\\S+\\s+"
            )
        ),
        FlexibleLiteralChoiceTerminal(
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
            "bpf-bind-network-interface"
        ),
        ZeroOrMore(
            SequenceCombinator(
                WhitespaceTerminal(),
                RegexTerminal("\\S+", "\\S+")
            )
        ),
        EOF()
    )
) {
    private companion object {
        /** cgroup_controller_table, as a regex alternation for the unknown-word lookahead. */
        const val CONTROLLER_NAMES = "cpu|cpuacct|cpuset|io|blkio|memory|devices|pids|bpf-firewall|bpf-devices|" +
            "bpf-foreign|bpf-socket-bind|bpf-restrict-network-interfaces|bpf-bind-network-interface"
    }
}
