package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for [Network] Gateway= (config_parse_route_section, ltype ROUTE_GATEWAY_NETWORK).
 *
 * systemd.network(5): "The gateway address, which must be in the format described in inet_pton(3).
 * This is a short-hand for a [Route] section only containing a Gateway= key."
 *
 * So it accepts an IPv4 or IPv6 address only — unlike [Route] Gateway=, the special "_dhcp4" /
 * "_ipv6ra" tokens are NOT accepted in the [Network] short-hand.
 */
class ConfigParseRouteSectionGatewayNetworkOptionValue : SimpleGrammarOptionValues(
    "config_parse_route_section",
    SequenceCombinator(
        AlternativeCombinator(IPV4_ADDR, IPV6_ADDR),
        EOF()
    )
)
