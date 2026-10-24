package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/*
 * [NetDev] MACAddress=, [Peer] MACAddress= (.netdev)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#MACAddress=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/netdev.c#L1264-L1286   config_parse_netdev_hw_addr
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L1864-L1893   config_parse_hw_addr
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/ether-addr-util.c#L196-L280   parse_hw_addr_full
 */

/**
 * Validator for [NetDev] MACAddress= and [Peer] MACAddress= (.netdev).
 *
 * C Function: config_parse_netdev_hw_addr (ltype ETH_ALEN). The exact word `none` (streq, so
 * case-sensitive) means "no address"; anything else goes to config_parse_hw_addr, i.e. a single 6-byte
 * Ethernet MAC. `none` was missing before (sd-check accepts it; `NONE` is refused).
 */
class ConfigParseNetdevHwAddrOptionValue : SimpleGrammarOptionValues(
  "config_parse_netdev_hw_addr",
  SequenceCombinator(
    AlternativeCombinator(
      MAC_ADDRESS,
      LiteralChoiceTerminal("none"),
    ),
    EOF(),
  ),
)
