package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Bond.AdActorSystemPriority
 * C Function: config_parse_ad_actor_sys_prio(0)
 * Used by Options: Bond.AdActorSystemPriority
 */
class ConfigParseAdActorSysPrioOptionValue : SimpleGrammarOptionValues(
    "config_parse_ad_actor_sys_prio",
    SequenceCombinator(
        IntegerTerminal(1, 65536),
        EOF()
    )
)
