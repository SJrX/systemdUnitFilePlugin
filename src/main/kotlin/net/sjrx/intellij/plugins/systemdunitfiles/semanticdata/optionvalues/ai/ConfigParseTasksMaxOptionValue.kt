package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for TasksMax=.
 *
 * C function: config_parse_tasks_max in src/core/load-fragment.c. The accepted values are:
 *   - "infinity"
 *   - a permyriad form: parse_permyriad accepts N%, N.N%, N.NN%, N‰, N.N‰, or N‱
 *     (bounded 0..10000). Only the ASCII percent form is matched here; the ‰/‱ Unicode
 *     suffixes are syntactically rare in unit files.
 *   - a strictly positive uint64 via safe_atou64 (>0 and <UINT64_MAX).
 *
 * Suffix-less "0" is rejected by the C parser (safe_atou64 returns 0 which fails the v<=0
 * guard); "0%" is accepted (permyriad 0). The grammar below reflects that asymmetry.
 */
class ConfigParseTasksMaxOptionValue : SimpleGrammarOptionValues(
    "config_parse_tasks_max",
    SequenceCombinator(
        AlternativeCombinator(
            FlexibleLiteralChoiceTerminal("infinity"),
            // Permyriad via %: 0..100 with up to two decimal places.
            SequenceCombinator(
                AlternativeCombinator(
                    SequenceCombinator(IntegerTerminal(0, 100), ZeroOrOne(RegexTerminal("\\.[0-9]{1,2}", "\\.[0-9]{1,2}"))),
                    SequenceCombinator(LiteralChoiceTerminal("100"), ZeroOrOne(RegexTerminal("\\.0{1,2}", "\\.0{1,2}")))
                ),
                LiteralChoiceTerminal("%")
            ),
            // Strictly positive integer.
            RegexTerminal("[0-9]+", "[1-9][0-9]*")
        ),
        EOF()
    )
)
