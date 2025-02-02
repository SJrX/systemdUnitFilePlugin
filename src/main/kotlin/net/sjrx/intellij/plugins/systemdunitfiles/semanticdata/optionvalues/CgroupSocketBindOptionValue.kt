package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import kotlinx.html.ADDRESS
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*



class CgroupSocketBindOptionValue() : GrammarOptionValue("config_parse_cgroup_socket_bind", GRAMMAR) {

  companion object {
    // https://www.freedesktop.org/software/systemd/man/latest/systemd.resource-control.html
    val ADDRESS_FAMILY = LiteralChoiceTerminal("ipv4", "ipv6")
    val TRANSPORT_PROTOCOL = LiteralChoiceTerminal("tcp", "udp")
    val IP_PORT = IntegerTerminal(1, 65536)
    val DASH = LiteralChoiceTerminal("-")
    val COLON = LiteralChoiceTerminal(":")
    val IP_PORT_RANGE = SequenceCombinator(IP_PORT, DASH, IP_PORT)
    val IP_PORTS = AlternativeCombinator(IP_PORT_RANGE, IP_PORT)

    val GRAMMAR = SequenceCombinator(
      AlternativeCombinator(
        LiteralChoiceTerminal("any"),
        // The grammar has three blocks address-family, transport-protocol, and ip-ports, if there is more than one, there is a colon.
        // We break this up into a few cases
        // All specified
        SequenceCombinator(ADDRESS_FAMILY, COLON, TRANSPORT_PROTOCOL,COLON, IP_PORTS),
        // No ports
        SequenceCombinator(ADDRESS_FAMILY, COLON, TRANSPORT_PROTOCOL),
        // No transport
        SequenceCombinator(ADDRESS_FAMILY, COLON, IP_PORTS),
        // No Address family
        SequenceCombinator(TRANSPORT_PROTOCOL,COLON, IP_PORTS),
        ADDRESS_FAMILY,
        TRANSPORT_PROTOCOL,
        IP_PORTS
      ),
      EOF())

    val validators = mapOf(
      Validator("config_parse_cgroup_socket_bind", "0") to CgroupSocketBindOptionValue()
    )
  }
}

