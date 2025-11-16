package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for CAKE.FirewallMark
 * C Function: config_parse_cake_fwmark(QDISC_KIND_CAKE)
 * Used by Options: CAKE.FirewallMark
 * 
 * Validates firewall mark values for CAKE queueing discipline.
 * Valid range: 1 to 4294967295 (positive 32-bit unsigned integer)
 */
class ConfigParseCakeFwmarkOptionValue : SimpleGrammarOptionValues(
    "config_parse_cake_fwmark",
    SequenceCombinator(
        IntegerTerminal(1, 4294967296),  // Range 1-4294967295 (max is exclusive)
        EOF()
    )
)
