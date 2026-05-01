package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for keys parsed by config_parse_ifname (e.g. Network.Bridge, Network.Bond,
 * Network.VRF, Network.BatmanAdvanced in .network; NetDev.Name, Peer.Name, VXCAN.Peer in
 * .netdev; Link.Name in .link; Network.Bridge in .nspawn).
 *
 * Delegates to ifname_valid, which requires a Linux interface name:
 *   - 1..15 characters
 *   - no whitespace, no '/', no ':', no '%' (rejected by ifname_valid_char)
 *   - cannot be "." or ".."
 *   - cannot be the reserved names "all" or "default"
 *   - cannot be a purely-numeric string (interpreted as ifindex)
 */
class ConfigParseIfnameOptionValue : SimpleGrammarOptionValues(
    "config_parse_ifname",
    SequenceCombinator(
        RegexTerminal(
            "(?!(?:all|default|\\.{1,2}|0[xX][0-9a-fA-F]+|[0-9]+)\\Z)[^\\s:/%]{1,15}",
            "(?!(?:all|default|\\.{1,2}|0[xX][0-9a-fA-F]+|[0-9]+)\\Z)[^\\s:/%]{1,15}"
        ),
        EOF()
    )
)
