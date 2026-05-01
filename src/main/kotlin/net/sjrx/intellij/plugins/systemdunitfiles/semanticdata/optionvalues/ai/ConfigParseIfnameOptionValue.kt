package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for keys parsed by config_parse_ifname (e.g. Network.Bridge, Network.Bond,
 * Network.VRF, Network.BatmanAdvanced in .network; NetDev.Name, Peer.Name, VXCAN.Peer in
 * .netdev; Link.Name in .link; Network.Bridge in .nspawn).
 *
 * The C function delegates to ifname_valid(), which requires a Linux interface name:
 *   - 1..15 characters
 *   - no whitespace, no '/'
 *   - cannot be "." or ".."
 *   - must be valid UTF-8 (effectively printable ASCII in practice)
 *
 * The grammar below is a reasonable approximation: 1..15 characters that are not
 * whitespace and not '/'. The "." / ".." edge cases are not modelled.
 */
class ConfigParseIfnameOptionValue : SimpleGrammarOptionValues(
    "config_parse_ifname",
    SequenceCombinator(
        RegexTerminal("[^\\s/]{1,15}", "[^\\s/]{1,15}"),
        EOF()
    )
)
