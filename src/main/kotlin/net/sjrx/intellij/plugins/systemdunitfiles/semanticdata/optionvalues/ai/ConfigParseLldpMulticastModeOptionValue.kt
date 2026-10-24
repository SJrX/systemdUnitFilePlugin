package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BOOLEAN_ANY_CASE
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Network] EmitLLDP= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#EmitLLDP=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-lldp-tx.c#L134-L174  config_parse_lldp_multicast_mode (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-lldp-tx.c#L126-L132  lldp_multicast_mode_table
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L23-L46            parse_boolean
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/23-emit-lldp.network#L7
 *        https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/26-bridge-vlan-slave-issue-20373.network#L9
 */

/**
 * Validator for `EmitLLDP=` (config_parse_lldp_multicast_mode): a boolean (case-insensitive; true means
 * `nearest-bridge`) or one of `nearest-bridge`, `non-tpmr-bridge`, `customer-bridge` (case-sensitive).
 */
class ConfigParseLldpMulticastModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_lldp_multicast_mode",
    AlternativeCombinator(
        SequenceCombinator(FlexibleLiteralChoiceTerminal("nearest-bridge", "non-tpmr-bridge", "customer-bridge"), EOF()),
        SequenceCombinator(BOOLEAN_ANY_CASE, EOF()),
    )
)
