package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Link.RequiredFamilyForOnline (.network)
 */
class ConfigParseRequiredFamilyForOnlineOptionValue : SimpleGrammarOptionValues(
    "config_parse_required_family_for_online",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("ipv4", "ipv6", "both", "any"),
        EOF()
    )
)
