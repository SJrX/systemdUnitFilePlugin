package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for [Neighbor] LinkLayerAddress= (and the deprecated [Neighbor] MACAddress= alias) (.network).
 * C Function: config_parse_neighbor_section (ltype NEIGHBOR_LINK_LAYER_ADDRESS) -> config_parse_hw_addr with
 * expected_len = 0: a single 4/6/16/20-byte hardware address, or an IPv4/IPv6 address literal.
 */
class ConfigParseNeighborLinkLayerAddressOptionValue : SimpleGrammarOptionValues(
    "config_parse_neighbor_section",
    SequenceCombinator(HARDWARE_ADDRESS, EOF())
)
