package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for VXLAN.TTL
 * C Function: config_parse_vxlan_ttl(0)
 * Used by Options: VXLAN.TTL
 */
class ConfigParseVxlanTtlOptionValue : SimpleGrammarOptionValues(
    "config_parse_vxlan_ttl",
    SequenceCombinator(
        AlternativeCombinator(
            LiteralChoiceTerminal("inherit"),
            IntegerTerminal(0, 256)
        ),
        EOF()
    )
)
