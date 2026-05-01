package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore

private val SECURE_BIT = FlexibleLiteralChoiceTerminal(
  "keep-caps",
  "keep-caps-locked",
  "no-setuid-fixup",
  "no-setuid-fixup-locked",
  "noroot",
  "noroot-locked"
)

/**
 * Validator for Service.SecureBits, Socket.SecureBits, Mount.SecureBits, Swap.SecureBits.
 *
 * C Function: config_parse_exec_secure_bits(0)
 *
 * Accepts a whitespace-separated list of secure bits flags. Valid values are:
 * keep-caps, keep-caps-locked, no-setuid-fixup, no-setuid-fixup-locked,
 * noroot, noroot-locked.
 */
class ConfigParseExecSecureBitsOptionValue : SimpleGrammarOptionValues(
  "config_parse_exec_secure_bits",
  SequenceCombinator(
    SECURE_BIT,
    ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), SECURE_BIT)),
    EOF()
  )
)
