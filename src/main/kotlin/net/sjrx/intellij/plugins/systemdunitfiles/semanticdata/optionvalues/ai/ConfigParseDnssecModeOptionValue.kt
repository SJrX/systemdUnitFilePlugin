package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.namesOrBoolean

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
    // dnssec_mode_table: the names are case-sensitive, the boolean half (parse_boolean) is not.
    namesOrBoolean("no", "allow-downgrade", "yes"),
    EOF()
  )
)
