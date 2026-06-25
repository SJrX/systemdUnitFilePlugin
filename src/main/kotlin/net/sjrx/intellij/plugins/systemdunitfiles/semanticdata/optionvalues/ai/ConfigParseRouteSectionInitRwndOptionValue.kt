package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Route.InitialAdvertisedReceiveWindow: a TCP window in 1..1023 (config_parse_tcp_window rejects 0
 * and values of 1024 or more).
 */
class ConfigParseRouteSectionInitRwndOptionValue : SimpleGrammarOptionValues(
    "config_parse_route_section",
    SequenceCombinator(IntegerTerminal(1, 1024), EOF())
)
