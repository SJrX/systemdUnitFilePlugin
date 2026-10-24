package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IFNAME_OR_IFINDEX
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [BridgeFDB] OutgoingInterface= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#OutgoingInterface=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-bridge-fdb.c#L485-L539  config_parse_fdb_interface (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L112-L124             parse_ifindex
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L785-L830            ifname_valid_full (IFNAME_VALID_ALTERNATIVE)
 */

/**
 * Validator for `[BridgeFDB] OutgoingInterface=` (config_parse_fdb_interface): a positive interface
 * index, or an alternative interface name of up to 127 characters — see [IFNAME_OR_IFINDEX].
 */
class ConfigParseFdbInterfaceOptionValue : SimpleGrammarOptionValues(
    "config_parse_fdb_interface",
    SequenceCombinator(IFNAME_OR_IFINDEX, EOF())
)
