package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Route.Protocol: "kernel", "boot", "static", or a number 0-255. systemd uses
 * route_protocol_from_string — a small string table with a numeric fallback up to UINT8_MAX.
 */
class ConfigParseRouteSectionProtocolOptionValue : SimpleGrammarOptionValues(
    "config_parse_route_section",
    SequenceCombinator(
        AlternativeCombinator(
            LiteralChoiceTerminal("kernel", "boot", "static"),
            IntegerTerminal(0, 256)
        ),
        EOF()
    )
)
