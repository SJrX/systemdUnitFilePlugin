package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BOOLEAN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/**
 * Validator for Network.DNSSEC (.network) and Resolve.DNSSEC (.conf via resolved.conf).
 *
 * C Function: config_parse_dnssec_mode(0)
 *
 * Backed by DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN(dnssec_mode, ...), so accepts
 * any boolean value as well as the explicit table entries: "no", "allow-downgrade", "yes".
 */
class ConfigParseDnssecModeOptionValue : SimpleGrammarOptionValues(
  "config_parse_dnssec_mode",
  SequenceCombinator(
    AlternativeCombinator(
      FlexibleLiteralChoiceTerminal("allow-downgrade"),
      BOOLEAN
    ),
    EOF()
  )
)
