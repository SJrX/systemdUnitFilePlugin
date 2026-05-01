package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV4_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV6_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IntegerTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

private val DNS_PORT = IntegerTerminal(0, 65536)
private val DNS_IFACE = RegexTerminal("[A-Za-z0-9_][A-Za-z0-9_.-]*", "[A-Za-z0-9_][A-Za-z0-9_.-]*")
private val DNS_IFACE_SUFFIX = SequenceCombinator(LiteralChoiceTerminal("%"), DNS_IFACE)

private val DNS_IPV4_ENTRY = SequenceCombinator(
  IPV4_ADDR,
  ZeroOrOne(SequenceCombinator(LiteralChoiceTerminal(":"), DNS_PORT)),
  ZeroOrOne(DNS_IFACE_SUFFIX)
)
private val DNS_IPV6_BRACKETED_ENTRY = SequenceCombinator(
  LiteralChoiceTerminal("["),
  IPV6_ADDR,
  LiteralChoiceTerminal("]"),
  ZeroOrOne(SequenceCombinator(LiteralChoiceTerminal(":"), DNS_PORT)),
  ZeroOrOne(DNS_IFACE_SUFFIX)
)
private val DNS_IPV6_BARE_ENTRY = SequenceCombinator(
  IPV6_ADDR,
  ZeroOrOne(DNS_IFACE_SUFFIX)
)
private val DNS_ENTRY = AlternativeCombinator(
  DNS_IPV6_BRACKETED_ENTRY,
  DNS_IPV4_ENTRY,
  DNS_IPV6_BARE_ENTRY
)

/**
 * Validator for Network.DNS (.network).
 *
 * C Function: config_parse_dns(0)
 *
 * Accepts a whitespace-separated list of DNS server addresses. Each entry is an IPv4
 * or IPv6 address. IPv4 addresses may optionally include a port suffix (":port") and
 * an interface suffix ("%iface"). IPv6 addresses can be bare, or bracketed with a port
 * ("[ipv6]:port") and may also have an interface suffix.
 */
class ConfigParseDnsOptionValue : SimpleGrammarOptionValues(
  "config_parse_dns",
  SequenceCombinator(
    DNS_ENTRY,
    ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), DNS_ENTRY)),
    EOF()
  )
)
