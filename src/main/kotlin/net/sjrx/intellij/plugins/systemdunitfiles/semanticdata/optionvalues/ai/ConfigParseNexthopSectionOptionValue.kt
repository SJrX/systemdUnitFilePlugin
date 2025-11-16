package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for NextHop.OnLink
 * C Function: config_parse_nexthop_section(NEXTHOP_ONLINK)
 * Used by Options: NextHop.OnLink
 * 
 * Takes a boolean value. If set to true, the kernel does not have to check if the gateway is
 * reachable directly by the current machine (i.e., attached to the local network), so that we
 * can insert the nexthop in the kernel table without it being complained about. Defaults to no.
 */
class ConfigParseNexthopSectionOptionValue : SimpleGrammarOptionValues(
    "config_parse_nexthop_section",
    SequenceCombinator(
        BOOLEAN,
        EOF()
    )
)
