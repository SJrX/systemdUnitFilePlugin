package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Bond.Mode
 * C Function: config_parse_bond_mode(0)
 * Used by Options: Bond.Mode
 */
class ConfigParseBondModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_bond_mode",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal(
            "balance-rr",
            "active-backup",
            "balance-xor",
            "broadcast",
            "802.3ad",
            "balance-tlb",
            "balance-alb"
        ),
        EOF()
    )
)
