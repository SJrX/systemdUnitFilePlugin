package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BOOLEAN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.PreferValidAlternativeCombinator
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

// namespace_flags_from_string() splits on whitespace; a leading '~' is cut off first, so whitespace may follow
// it, and "~" alone is an empty (inverted) list.
// https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L3579-L3643
// https://github.com/systemd/systemd/blob/0f0682b773/src/shared/nsflags.c#L10-L41
private val NAMESPACE_LIST = AlternativeCombinator(
  SequenceCombinator(
    LiteralChoiceTerminal("~"),
    ZeroOrOne(WhitespaceTerminal()),
    ZeroOrOne(SequenceCombinator(NAMESPACE_TYPE, ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), NAMESPACE_TYPE)))),
  ),
  SequenceCombinator(NAMESPACE_TYPE, ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), NAMESPACE_TYPE))),
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
    // PreferValid: NAMESPACE_TYPE's lenient lower-case shape would otherwise take the `t` out of a
    // mixed-case boolean like `tRUE` (parse_boolean ignores case) and strand the rest.
    PreferValidAlternativeCombinator(
      NAMESPACE_LIST,
      BOOLEAN
    ),
    EOF()
  )
)
