package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for ManagedOOMMemoryPressureLimit=.
 *
 * C function: config_parse_managed_oom_mem_pressure_limit in src/core/load-fragment.c. After
 * a "supported for this unit type" check it calls parse_permyriad(rvalue), which accepts
 * N(.NN)?% / N(.N)?‰ / N‱. Only the ASCII percent form is matched here (the Unicode forms are
 * vanishingly rare in unit files).
 */
class ConfigParseManagedOomMemPressureLimitOptionValue : SimpleGrammarOptionValues(
    "config_parse_managed_oom_mem_pressure_limit",
    SequenceCombinator(
        AlternativeCombinator(
            // 0..99 with optional one or two decimal places, followed by %
            SequenceCombinator(IntegerTerminal(0, 100), ZeroOrOne(RegexTerminal("\\.[0-9]{1,2}", "\\.[0-9]{1,2}")), LiteralChoiceTerminal("%")),
            // Exactly 100% or 100.0% / 100.00%
            SequenceCombinator(LiteralChoiceTerminal("100"), ZeroOrOne(RegexTerminal("\\.0{1,2}", "\\.0{1,2}")), LiteralChoiceTerminal("%"))
        ),
        EOF()
    )
)
