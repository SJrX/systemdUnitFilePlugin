package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for SmackProcessLabel=.
 *
 * C function: config_parse_exec_smack_process_label in src/core/load-fragment.c. Identical shape
 * to SELinuxContext=: an optional leading '-' marks "ignore failures"; the remainder is run
 * through unit_full_printf() and stored verbatim with no constraint on the label string. The only
 * meaningful check is specifier validity ('%' must be '%%' or a valid unit specifier). The empty
 * value resets the field.
 */
class ConfigParseExecSmackProcessLabelOptionValue : SimpleGrammarOptionValues(
    "config_parse_exec_smack_process_label",
    SequenceCombinator(
        RegexTerminal(".*", "-?(?:[^%]|%%|%[iIjJnNpPfyYcrRCdDELSthsaAbBHlqmMovwWgGuUTV])*"),
        EOF()
    )
)
