package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/**
 * Validator for config_parse_dns_name(0).
 *
 * Used by .network DHCPServer.Domain, DHCPServer.BootServerName, DHCPServer.LocalLeaseDomain.
 *
 * Mirrors dns_name_is_valid (no DNS_LABEL_LDH flag): each label is a non-empty
 * sequence of any characters except '.' (label separator) and '\' (escape
 * introducer); labels are joined by single dots; an optional trailing dot
 * denotes a fully-qualified name. Empty labels (e.g. ".." or a leading ".")
 * are rejected. This is intentionally far more permissive than hostname
 * validation — leading/trailing hyphens, underscores, spaces, and most
 * printable punctuation are all accepted by the C validator.
 */
class ConfigParseDnsNameOptionValue : SimpleGrammarOptionValues(
  "config_parse_dns_name",
  SequenceCombinator(
    RegexTerminal(
      "[^.\\\\]+(?:\\.[^.\\\\]+)*\\.?",
      "[^.\\\\]+(?:\\.[^.\\\\]+)*\\.?"
    ),
    EOF()
  )
)
