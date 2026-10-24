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
 * A word that uses a known specifier (e.g. %I) is only known once it is expanded, which depends on the unit name, so
 * it is accepted; an unknown specifier (e.g. %Q) makes unit_full_printf() refuse the whole value
 * (https://github.com/systemd/systemd/blob/0f0682b773/src/shared/specifier.c#L33-L105). Every other word goes
 * through parse_range() (safe_atou(), base 0, so "0x3" and "+1" are numbers) and must give lower <= upper below
 * CPU_SET_MAX_NCPU (8192):
 * https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L305-L340
 * https://github.com/systemd/systemd/blob/0f0682b773/src/shared/cpu-set-util.c#L186-L206
 * A list of separators only ("," or " , ") is an empty set, which is fine.
 */
class ConfigParseExecCpuAffinityOptionValue : SimpleGrammarOptionValues(
    "config_parse_exec_cpu_affinity",
    SequenceCombinator(
        AlternativeCombinator(
            LiteralChoiceTerminal("numa"),
            SequenceCombinator(
                ZeroOrOne(RegexTerminal("[\\s,]+", "[\\s,]+")),
                ZeroOrOne(
                    SequenceCombinator(
                        AlternativeCombinator(SPECIFIER_WORD, CPU_RANGE),
                        ZeroOrMore(SequenceCombinator(
                            RegexTerminal("[\\s,]+", "[\\s,]+"),
                            AlternativeCombinator(SPECIFIER_WORD, CPU_RANGE)
                        )),
                        ZeroOrOne(RegexTerminal("[\\s,]+", "[\\s,]+")),
                    )
                ),
            )
        ),
        EOF()
    )
) {
    companion object {
        // A word with a known specifier and no unknown one.
        private val SPECIFIER_WORD = RegexTerminal(
            "[^\\s,]*%[^\\s,]*",
            "(?=(?:[^\\s,%]|%%|%(?![A-Za-z0-9%]))*%[${BatchUnitCoreGrammar.UNIT_SPECIFIER_LETTERS}])" +
                "(?:[^\\s,%]|%%|%[${BatchUnitCoreGrammar.UNIT_SPECIFIER_LETTERS}]|%(?![A-Za-z0-9%]))+"
        )

        // `N` or `N-M`, checked like parse_range() and cpu_set_add_range().
        private val CPU_RANGE = ValueChecked(RegexTerminal("[^\\s,%]+", "[^\\s,%]+")) { text -> cpuRangeIsAccepted(text) }

        // safe_atou(): base 0, an optional '+', and "-0" is 0.
        private val CPU_NUMBER = SafeAtoTerminal(
            signed = false,
            base = 0,
            flags = emptySet(),
            minInclusive = 0L,
            maxInclusive = 4_294_967_295L,
            acceptsWhitespace = true,
            acceptsMinus = true,
        )

        private const val CPU_SET_MAX_NCPU = 8192

        private fun cpuRangeIsAccepted(raw: String): Boolean {
            // extract_first_word(…, EXTRACT_UNQUOTE): drop quotes, a backslash keeps the next character.
            val word = StringBuilder()
            var quote = 0.toChar()
            var i = 0
            while (i < raw.length) {
                val c = raw[i]
                if (c == '\\' && i + 1 < raw.length) {
                    word.append(raw[i + 1])
                    i += 2
                    continue
                }
                if (quote == 0.toChar() && (c == '"' || c == '\'')) {
                    quote = c
                } else if (c == quote) {
                    quote = 0.toChar()
                } else {
                    word.append(c)
                }
                i++
            }
            if (quote != 0.toChar()) {
                return false
            }

            val text = word.toString()
            val dash = text.indexOf('-')
            val lower = CPU_NUMBER.valueOf(if (dash < 0) text else text.substring(0, dash)) ?: return false
            val upper = if (dash < 0) lower else CPU_NUMBER.valueOf(text.substring(dash + 1)) ?: return false
            return lower <= upper && upper < CPU_SET_MAX_NCPU.toBigInteger()
        }
    }
}
