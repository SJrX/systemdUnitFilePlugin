package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for CPUAffinity=.
 *
 * C function: config_parse_exec_cpu_affinity in src/core/load-fragment.c. Either the literal
 * "numa" (use NUMA-derived CPU set) or a CPU set list parsed by the same code path as
 * NUMAMask= / AllowedCPUs=: whitespace- or comma-separated integers and `N-M` ranges.
 *
 * The "numa" literal goes first explicitly; otherwise the CPU set grammar matches integers
 * and ranges. Note the existing AllowedCpuSetOptionValue handles the same syntax for
 * config_parse_unit_cpu_set — duplicated here as a grammar so it composes with the "numa"
 * alternative.
 */
class ConfigParseExecCpuAffinityOptionValue : SimpleGrammarOptionValues(
    "config_parse_exec_cpu_affinity",
    SequenceCombinator(
        AlternativeCombinator(
            LiteralChoiceTerminal("numa"),
            SequenceCombinator(
                CPU_RANGE,
                ZeroOrMore(SequenceCombinator(
                    AlternativeCombinator(WhitespaceTerminal(), LiteralChoiceTerminal(",")),
                    CPU_RANGE
                ))
            )
        ),
        EOF()
    )
) {
    companion object {
        // `N` or `N-M` (each side a non-negative integer)
        private val CPU_RANGE = RegexTerminal("[0-9]+(-[0-9]+)?", "[0-9]+(-[0-9]+)?")
    }
}
