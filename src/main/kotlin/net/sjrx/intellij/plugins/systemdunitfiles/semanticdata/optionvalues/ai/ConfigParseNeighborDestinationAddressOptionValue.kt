package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IP_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Neighbor] Address= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BNeighbor%5D%20Section%20Options
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-neighbor.c#L747-L781   config_parse_neighbor_section (NEIGHBOR_DESTINATION_ADDRESS)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L2084-L2112      config_parse_in_addr_data
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L531-L551       in_addr_from_string_auto
 * linux  https://man7.org/linux/man-pages/man3/inet_pton.3.html
 */

/**
 * Validator for `[Neighbor] Address=` (.network) — the NEIGHBOR_DESTINATION_ADDRESS slot of
 * config_parse_neighbor_section, whose table entry is config_parse_in_addr_data.
 *
 * That reads one address of either family with in_addr_from_string_auto (inet_pton(AF_INET), then
 * inet_pton(AF_INET6)): no prefix length and no `%zone`. Unlike some address settings it does not
 * refuse the ANY address at parse time, so neither does this grammar.
 */
class ConfigParseNeighborDestinationAddressOptionValue : SimpleGrammarOptionValues(
    "config_parse_neighbor_section",
    SequenceCombinator(IP_ADDR, EOF())
)
