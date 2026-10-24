package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [VLAN] Protocol= and [Bridge] VLANProtocol= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#Protocol=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L1832-L1862   config_parse_vlanprotocol (0)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-bridge.netdev#L17
 */

/**
 * Validator for `[VLAN] Protocol=` and `[Bridge] VLANProtocol=` (.netdev).
 *
 * C function: config_parse_vlanprotocol, which compares the value with STR_IN_SET against exactly four
 * spellings: `802.1q` / `802.1Q` (ETH_P_8021Q) and `802.1ad` / `802.1AD` (ETH_P_8021AD). Mixed case
 * such as `802.1Ad` is refused. An empty assignment resets to the kernel default.
 */
class ConfigParseVlanprotocolOptionValue : SimpleGrammarOptionValues(
    "config_parse_vlanprotocol",
    SequenceCombinator(FlexibleLiteralChoiceTerminal("802.1q", "802.1Q", "802.1ad", "802.1AD"), EOF())
)
