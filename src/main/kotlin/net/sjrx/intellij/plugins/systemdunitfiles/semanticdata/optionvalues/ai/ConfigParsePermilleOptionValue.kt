package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for permille (parts-per-thousand) options such as CAN.SamplePoint=.
 *
 * C function: config_parse_permille in src/shared/conf-parser.c → parse_permille in
 * src/basic/percent-util.c. Accepts either:
 *   - "N‰" (integer 0..1000)
 *   - "N%" or "N.x%" (0..100% via tenths place, internally translated to 0..1000)
 *
 * Only the ASCII percent form is matched here; the ‰ Unicode suffix is syntactically rare.
 */
class ConfigParsePermilleOptionValue : SimpleGrammarOptionValues(
    "config_parse_permille",
    SequenceCombinator(
        AlternativeCombinator(
            // 0..99(.x)?%
            SequenceCombinator(IntegerTerminal(0, 100), ZeroOrOne(RegexTerminal("\\.[0-9]", "\\.[0-9]")), LiteralChoiceTerminal("%")),
            // 100% or 100.0%
            SequenceCombinator(LiteralChoiceTerminal("100"), ZeroOrOne(LiteralChoiceTerminal(".0")), LiteralChoiceTerminal("%"))
        ),
        EOF()
    )
)
