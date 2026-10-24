package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore

private val SECURE_BIT = FlexibleLiteralChoiceTerminal(
  "keep-caps",
  "keep-caps-locked",
  "no-setuid-fixup",
  "no-setuid-fixup-locked",
  "noroot",
  "noroot-locked",
  "no-cap-ambient-raise",
  "no-cap-ambient-raise-locked",
  "exec-restrict-file",
  "exec-restrict-file-locked",
  "exec-deny-interactive",
  "exec-deny-interactive-locked"
)

/**
 * Validator for Service.SecureBits, Socket.SecureBits, Mount.SecureBits, Swap.SecureBits.
 *
 * C Function: config_parse_exec_secure_bits(0) → secure_bits_from_string(),
 * https://github.com/systemd/systemd/blob/0f0682b773/src/shared/securebits-util.c#L93-L134
 *
 * A whitespace-separated list of secure bits flags. secure_bits_from_string() skips words it does not know without
 * an error, so systemd's parser accepts any word; the known flags are offered for completion.
 */
class ConfigParseExecSecureBitsOptionValue : SimpleGrammarOptionValues(
  "config_parse_exec_secure_bits",
  SequenceCombinator(
    AlternativeCombinator(RegexTerminal("\\S+", "\\S+"), SECURE_BIT),
    ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), AlternativeCombinator(RegexTerminal("\\S+", "\\S+"), SECURE_BIT))),
    EOF()
  )
)
