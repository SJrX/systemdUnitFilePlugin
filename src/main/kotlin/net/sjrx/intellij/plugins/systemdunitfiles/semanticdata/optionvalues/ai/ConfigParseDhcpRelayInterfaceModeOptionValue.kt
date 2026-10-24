package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Network] DHCPRelay= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#DHCPRelay=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-relay.c#L376-L380  config_parse_dhcp_relay_interface_mode (0), DEFINE_CONFIG_PARSE_ENUM_WITH_DEFAULT
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-relay.c#L369-L372  dhcp_relay_interface_mode_table
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-dhcp-relay-downstream.network
 */

/**
 * Validator for `[Network] DHCPRelay=`: `upstream` or `downstream` (case-sensitive, no boolean). The
 * empty assignment resets to "unset" and never reaches the grammar.
 */
class ConfigParseDhcpRelayInterfaceModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_relay_interface_mode",
    SequenceCombinator(FlexibleLiteralChoiceTerminal("upstream", "downstream"), EOF())
)
