package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BOOLEAN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/**
 * Validator for SR-IOV.LinkState in .network and .link files.
 *
 * C Function: config_parse_sr_iov_link_state(0)
 *
 * Accepts the literal "auto", or any boolean value (which is parsed via parse_boolean
 * and mapped to SR_IOV_LINK_STATE_ENABLE / SR_IOV_LINK_STATE_DISABLE).
 */
class ConfigParseSrIovLinkStateOptionValue : SimpleGrammarOptionValues(
  "config_parse_sr_iov_link_state",
  SequenceCombinator(
    AlternativeCombinator(
      BOOLEAN,
      FlexibleLiteralChoiceTerminal("auto")
    ),
    EOF()
  )
)
