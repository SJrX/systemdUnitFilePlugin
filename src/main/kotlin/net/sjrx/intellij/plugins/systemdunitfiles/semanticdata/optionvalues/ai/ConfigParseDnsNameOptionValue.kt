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
 * Mirrors dns_name_is_valid(s) (no DNS_LABEL_LDH flag), which validates via
 * dns_name_concat -> dns_label_unescape. Without the LDH flag, label characters
 * are more permissive than hostname_is_valid (e.g. underscores are allowed and
 * leading/trailing hyphens are not specifically rejected). A single optional
 * trailing dot is accepted to denote a fully-qualified name.
 *
 * Note: dns_label_unescape also supports backslash-escaped characters and
 * decimal-escaped octets ("\NNN"); these are extremely rare in DHCP server
 * configuration values and are intentionally not modelled here.
 */
class ConfigParseDnsNameOptionValue : SimpleGrammarOptionValues(
  "config_parse_dns_name",
  SequenceCombinator(
    RegexTerminal(
      "[a-zA-Z0-9_](?:[a-zA-Z0-9_-]*[a-zA-Z0-9_])?(?:\\.[a-zA-Z0-9_](?:[a-zA-Z0-9_-]*[a-zA-Z0-9_])?)*\\.?",
      "[a-zA-Z0-9_](?:[a-zA-Z0-9_-]*[a-zA-Z0-9_])?(?:\\.[a-zA-Z0-9_](?:[a-zA-Z0-9_-]*[a-zA-Z0-9_])?)*\\.?"
    ),
    EOF()
  )
)
