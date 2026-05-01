package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BOOLEAN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

private val NAMESPACE_TYPE = FlexibleLiteralChoiceTerminal(
  "cgroup",
  "ipc",
  "net",
  "mnt",
  "pid",
  "user",
  "uts",
  "time"
)

private val NAMESPACE_LIST = SequenceCombinator(
  ZeroOrOne(LiteralChoiceTerminal("~")),
  NAMESPACE_TYPE,
  ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), NAMESPACE_TYPE))
)

/**
 * Validator for Service.RestrictNamespaces, Service.DelegateNamespaces, Socket.RestrictNamespaces,
 * Socket.DelegateNamespaces, Mount.RestrictNamespaces, Mount.DelegateNamespaces, Swap.RestrictNamespaces,
 * Swap.DelegateNamespaces.
 *
 * C Function: config_parse_namespace_flags(0)
 *
 * Accepts either a boolean (yes/no/on/off/...) or a whitespace-separated list of namespace types,
 * optionally preceded by '~' to invert the meaning. Valid namespace types are: cgroup, ipc, net,
 * mnt, pid, user, uts, time.
 */
class ConfigParseNamespaceFlagsOptionValue : SimpleGrammarOptionValues(
  "config_parse_namespace_flags",
  SequenceCombinator(
    AlternativeCombinator(
      NAMESPACE_LIST,
      BOOLEAN
    ),
    EOF()
  )
)
