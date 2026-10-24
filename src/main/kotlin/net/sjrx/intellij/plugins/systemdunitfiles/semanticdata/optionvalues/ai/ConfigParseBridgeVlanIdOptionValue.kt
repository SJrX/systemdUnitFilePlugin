package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BOOLEAN_FALSE
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [BridgeVLAN] PVID= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#PVID=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-bridge-vlan.c#L341-L381  config_parse_bridge_vlan_id (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/vlan-util.c#L8-L23                parse_vlanid (safe_atou16, <= VLANID_MAX = 4094)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L23-L46               parse_boolean
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/26-bridge-vlan-master.network.d/30-override.conf#L3
 *        https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/26-bridge-vlan-master.network#L9
 */

/**
 * Validator for `[BridgeVLAN] PVID=` (config_parse_bridge_vlan_id): a *false* boolean (removes the PVID;
 * a true one falls through to parse_vlanid and is refused), or a VLAN id 0…4094 read by safe_atou16
 * (base 0, so `0x10` is legal).
 */
class ConfigParseBridgeVlanIdOptionValue : SimpleGrammarOptionValues(
    "config_parse_bridge_vlan_id",
    AlternativeCombinator(
        SequenceCombinator(
            SafeAtoTerminal(
                signed = false,
                base = 0,
                flags = emptySet(),
                minInclusive = 0L,
                maxInclusive = 4_094L,
                acceptsWhitespace = true,
                acceptsMinus = true,
            ),
            EOF(),
        ),
        SequenceCombinator(BOOLEAN_FALSE, EOF()),
    )
)
