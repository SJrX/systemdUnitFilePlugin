package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.PreferValidAlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [FooOverUDP] Protocol= (.netdev)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#Protocol=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L2257-L2287   config_parse_ip_protocol (ltype true = relaxed)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/ip-protocol-list.c#L36-L70   parse_ip_protocol_full
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L497-L520   safe_atoi
 */

/**
 * Validator for [FooOverUDP] Protocol= (.netdev).
 *
 * C Function: config_parse_ip_protocol(true). parse_ip_protocol_full(…, relaxed = true) accepts an
 * IPPROTO_* name, looked up case-insensitively (so `GRE` and `ipip` work; see [IP_PROTOCOL_NAMES]), or a
 * number read with safe_atoi() (base 0, so `0x2f` works) that is not negative. Being relaxed, it does not
 * require a name for the number, so `3` is accepted here (unlike [RoutingPolicyRule] IPProtocol=).
 * config_parse_ip_protocol then refuses anything above 255, which also rules out the names `smc` (256)
 * and `mptcp` (262). All confirmed with sd-check at 0f0682b773.
 */
class ConfigParseIpProtocolOptionValue : SimpleGrammarOptionValues(
  "config_parse_ip_protocol",
  SequenceCombinator(
    PreferValidAlternativeCombinator(
      FlexibleLiteralChoiceTerminal(*IP_PROTOCOL_NAMES, ignoreCase = true),
      SafeAtoTerminal(
        signed = true,
        base = 0,
        flags = emptySet(),
        minInclusive = 0L,
        maxInclusive = 255L,
        acceptsWhitespace = true,
        acceptsMinus = true,
      ),
    ),
    EOF(),
  ),
)
