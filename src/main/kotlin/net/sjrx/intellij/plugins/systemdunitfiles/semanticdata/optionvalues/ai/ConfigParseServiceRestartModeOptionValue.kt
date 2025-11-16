package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Service.RestartMode
 * C Function: config_parse_service_restart_mode(0)
 * Used by Options: Service.RestartMode
 *
 * Validates restart mode values:
 * - normal: Service restarts through failed/inactive state (default)
 * - direct: Service transitions directly to activating state, skipping failed/inactive
 * - debug: Service logs debug messages during automated restarts
 */
class ConfigParseServiceRestartModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_service_restart_mode",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("normal", "direct", "debug"),
        EOF()
    )
)
