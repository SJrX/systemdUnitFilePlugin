package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for job-mode options (e.g. OnFailureJobMode=, OnSuccessJobMode=).
 *
 * C function: config_parse_job_mode, expanded via DEFINE_CONFIG_PARSE_ENUM in
 * src/core/load-fragment.c. Accepts exactly the entries of job_mode_table in
 * src/basic/unit-def.c.
 */
class ConfigParseJobModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_job_mode",
    SequenceCombinator(
        LiteralChoiceTerminal(
            "fail",
            "lenient",
            "replace",
            "replace-irreversibly",
            "isolate",
            "flush",
            "ignore-dependencies",
            "ignore-requirements",
            "triggering",
            "restart-dependencies"
        ),
        EOF()
    )
)
