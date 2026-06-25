package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for [Route] Gateway= (config_parse_route_section, ltype ROUTE_GATEWAY).
 *
 * systemd.network(5): "Takes the gateway address or the special values "_dhcp4" and "_ipv6ra". If
 * "_dhcp4" or "_ipv6ra" is set, then the gateway address provided by DHCPv4 or IPv6 RA is used."
 */
class ConfigParseRouteSectionGatewayOptionValue : SimpleGrammarOptionValues(
    "config_parse_route_section",
    SequenceCombinator(
        AlternativeCombinator(
            IPV4_ADDR,
            IPV6_ADDR,
            LiteralChoiceTerminal("_dhcp4"),
            LiteralChoiceTerminal("_ipv6ra")
        ),
        EOF()
    )
)
