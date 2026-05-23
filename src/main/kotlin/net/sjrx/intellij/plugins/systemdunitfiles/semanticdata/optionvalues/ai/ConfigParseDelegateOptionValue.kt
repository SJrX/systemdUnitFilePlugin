package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for Delegate=.
 *
 * C function: config_parse_delegate in src/core/load-fragment.c. Either a boolean (toggles
 * delegation for all controllers) or a whitespace-separated list of cgroup controller names
 * from cgroup_controller_table in src/basic/cgroup-util.c.
 *
 * Note: the controller list is the first alternative because BOOLEAN is a
 * FlexibleLiteralChoiceTerminal whose syntactic phase accepts any short alphanumeric token —
 * if it ran first, single-controller inputs like "cpu" would syntactically consume the value
 * as a (fake) boolean and AlternativeCombinator would short-circuit there.
 */
class ConfigParseDelegateOptionValue : SimpleGrammarOptionValues(
    "config_parse_delegate",
    SequenceCombinator(
        AlternativeCombinator(
            SequenceCombinator(
                LiteralChoiceTerminal(
                    "cpu", "cpuacct", "cpuset", "io", "blkio", "memory", "devices", "pids",
                    "bpf-firewall", "bpf-devices", "bpf-foreign", "bpf-socket-bind",
                    "bpf-restrict-network-interfaces", "bpf-bind-network-interface"
                ),
                ZeroOrMore(SequenceCombinator(
                    WhitespaceTerminal(),
                    LiteralChoiceTerminal(
                        "cpu", "cpuacct", "cpuset", "io", "blkio", "memory", "devices", "pids",
                        "bpf-firewall", "bpf-devices", "bpf-foreign", "bpf-socket-bind",
                        "bpf-restrict-network-interfaces", "bpf-bind-network-interface"
                    )
                ))
            ),
            BOOLEAN
        ),
        EOF()
    )
)
