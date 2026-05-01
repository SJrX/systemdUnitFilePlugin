package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for FairQueueing.Pacing (.network)
 * C function: config_parse_fq_bool
 *
 * The C implementation calls parse_tristate(rvalue, &fq->pacing). Since the
 * "third" argument is NULL, an empty string sets the value to -1 (auto) and
 * any other value is parsed via parse_boolean (yes/y/true/t/on/1 or no/n/false/f/off/0).
 * Empty values are handled by the framework, so we only validate boolean.
 */
class ConfigParseFairQueueingBoolOptionValue : SimpleGrammarOptionValues(
    "config_parse_fq_bool",
    SequenceCombinator(
        BOOLEAN,
        EOF()
    )
)
