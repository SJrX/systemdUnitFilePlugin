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
        // specifier_printf() only refuses an unknown ALPHANUMERIC specifier (-EBADSLT); a '%' followed by any other
        // character, or at the very end, is kept as it is.
        // https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L1995-L2041
        // https://github.com/systemd/systemd/blob/0f0682b773/src/shared/specifier.c#L31-L105
        RegexTerminal(".*", "-?(?:[^%]|%%|%[iIjJnNpPfyYcrRCdDELSthsaAbBHlqmMovwWgGuUTV]|%(?![A-Za-z0-9%]))*"),
        EOF()
    )
)
