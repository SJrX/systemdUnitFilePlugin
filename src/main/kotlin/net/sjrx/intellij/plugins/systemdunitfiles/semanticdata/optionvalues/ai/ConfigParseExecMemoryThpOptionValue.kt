package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for MemoryTHP=.
 *
 * C function: config_parse_exec_memory_thp, expanded via DEFINE_CONFIG_PARSE_ENUM in
 * src/core/load-fragment.c. Accepts exactly the entries of exec_memory_thp_table in
 * src/core/execute.c.
 */
class ConfigParseExecMemoryThpOptionValue : SimpleGrammarOptionValues(
    "config_parse_exec_memory_thp",
    SequenceCombinator(
        LiteralChoiceTerminal("inherit", "disable", "madvise", "system"),
        EOF()
    )
)
