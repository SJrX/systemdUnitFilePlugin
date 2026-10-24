package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for CPUAffinity=.
 *
 * C function: config_parse_exec_cpu_affinity,
 * https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L1834-L1864. Either the literal
 * "numa" or a CPU set: config_parse_unit_cpu_set() expands specifiers in the whole value, then
 * config_parse_cpu_set() splits it with extract_first_word(WHITESPACE ",") — leading, trailing and repeated
 * separators are fine — and reads each word as `N` or `N-M`
 * (https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L3790-L3819,
 * https://github.com/systemd/systemd/blob/0f0682b773/src/shared/cpu-set-util.c#L220-L300).
 *
 * A word containing a specifier (e.g. %I) is only known once it is expanded, which depends on the unit name, so it
 * is accepted.
 */
class ConfigParseExecCpuAffinityOptionValue : SimpleGrammarOptionValues(
    "config_parse_exec_cpu_affinity",
    SequenceCombinator(
        AlternativeCombinator(
            LiteralChoiceTerminal("numa"),
            SequenceCombinator(
                ZeroOrOne(RegexTerminal("[\\s,]+", "[\\s,]+")),
                AlternativeCombinator(RegexTerminal("[^\\s,]*%[^\\s,]*", "[^\\s,]*%[^\\s,]*"), CPU_RANGE),
                ZeroOrMore(SequenceCombinator(
                    RegexTerminal("[\\s,]+", "[\\s,]+"),
                    AlternativeCombinator(RegexTerminal("[^\\s,]*%[^\\s,]*", "[^\\s,]*%[^\\s,]*"), CPU_RANGE)
                )),
                ZeroOrOne(RegexTerminal("[\\s,]+", "[\\s,]+")),
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
