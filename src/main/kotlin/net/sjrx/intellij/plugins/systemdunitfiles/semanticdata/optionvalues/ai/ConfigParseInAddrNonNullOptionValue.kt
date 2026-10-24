package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/*
 * config_parse_in_addr_non_null with ltype AF_INET, e.g. [DHCPServer] RelayTarget= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#RelayTarget=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L2036-L2082   config_parse_in_addr_non_null
 */

/**
 * Validator for config_parse_in_addr_non_null(AF_INET), e.g. [DHCPServer] RelayTarget=.
 *
 * An IPv4 address in inet_pton(3) form that is not the ANY address: `0.0.0.0` is refused ("cannot be the
 * ANY address", sd-check at 0f0682b773). Same check as [ConfigParseInAddrNonNullIpv6OptionValue].
 */
class ConfigParseInAddrNonNullOptionValue : SimpleGrammarOptionValues(
  "config_parse_in_addr_non_null",
  SequenceCombinator(
    Checked(IPV4_ADDR) { text -> ipLiteralBytes(text)?.let { !ipIsNull(it) } ?: true },
    EOF(),
  ),
)
