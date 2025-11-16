package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Route.Type
 * C Function: config_parse_route_section(ROUTE_TYPE)
 * Used by Options: Route.Type
 * 
 * Validates route type values which specify how the route should behave.
 * Valid values include unicast (regular route), local, broadcast, anycast,
 * multicast, blackhole (discard silently), unreachable (discard with ICMP),
 * prohibit (discard with admin prohibited ICMP), throw (fail lookup),
 * nat, and xresolve.
 */
class ConfigParseRouteSectionOptionValue : SimpleGrammarOptionValues(
    "config_parse_route_section",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal(
            "unicast",
            "unreachable",
            "blackhole",
            "prohibit",
            "broadcast",
            "multicast",
            "xresolve",
            "anycast",
            "throw",
            "local",
            "nat"
        ),
        EOF()
    )
)
