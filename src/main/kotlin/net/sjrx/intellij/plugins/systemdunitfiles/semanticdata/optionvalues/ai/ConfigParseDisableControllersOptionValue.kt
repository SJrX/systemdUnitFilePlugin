package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Swap.DisableControllers
 * C Function: config_parse_disable_controllers(0)
 * Used by Options: Swap.DisableControllers
 */
class ConfigParseDisableControllersOptionValue : SimpleGrammarOptionValues(
    "config_parse_disable_controllers",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal(
            "cpu",
            "cpuset",
            "io",
            "memory",
            "pids",
            "bpf-firewall",
            "bpf-devices",
            "bpf-foreign",
            "bpf-socket-bind",
            "bpf-restrict-network-interfaces"
        ),
        ZeroOrMore(
            SequenceCombinator(
                WhitespaceTerminal(),
                FlexibleLiteralChoiceTerminal(
                    "cpu",
                    "cpuset",
                    "io",
                    "memory",
                    "pids",
                    "bpf-firewall",
                    "bpf-devices",
                    "bpf-foreign",
                    "bpf-socket-bind",
                    "bpf-restrict-network-interfaces"
                )
            )
        ),
        EOF()
    )
)
