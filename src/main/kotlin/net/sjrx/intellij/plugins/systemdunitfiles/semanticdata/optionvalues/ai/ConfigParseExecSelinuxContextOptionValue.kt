package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for SELinuxContext=.
 *
 * C function: config_parse_exec_selinux_context in src/core/load-fragment.c. An optional leading
 * '-' marks the assignment as "ignore failures"; the remainder is run through unit_full_printf()
 * (specifier expansion) and stored verbatim -- there is no constraint on the context string
 * itself. So, as with config_parse_unit_string_printf, the only meaningful check is specifier
 * validity: every '%' must be '%%' or a valid unit specifier. The empty value resets the field.
 */
class ConfigParseExecSelinuxContextOptionValue : SimpleGrammarOptionValues(
    "config_parse_exec_selinux_context",
    SequenceCombinator(
        RegexTerminal(".*", "-?(?:[^%]|%%|%[iIjJnNpPfyYcrRCdDELSthsaAbBHlqmMovwWgGuUTV])*"),
        EOF()
    )
)
