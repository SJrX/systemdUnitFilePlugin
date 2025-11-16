package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Unit.OnFailureIsolate
 * C Function: config_parse_job_mode_isolate(0)
 * Used by Options: Unit.OnFailureIsolate
 * 
 * This is a deprecated option that has been renamed to OnFailureJobMode.
 * It accepts boolean values that determine the job mode:
 * - true/yes/on/1/y/t → JOB_ISOLATE
 * - false/no/off/0/n/f → JOB_REPLACE
 */
class ConfigParseJobModeIsolateOptionValue : SimpleGrammarOptionValues(
    "config_parse_job_mode_isolate",
    SequenceCombinator(
        BOOLEAN,
        EOF()
    )
)
