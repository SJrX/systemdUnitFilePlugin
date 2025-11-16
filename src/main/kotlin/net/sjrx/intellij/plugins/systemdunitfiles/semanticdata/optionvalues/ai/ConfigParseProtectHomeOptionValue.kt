package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Swap.ProtectHome
 * C Function: config_parse_protect_home(0)
 * Used by Options: Swap.ProtectHome
 * 
 * Validates values for ProtectHome= option which accepts:
 * - Boolean values (yes, no, true, false, on, off, 1, 0, y, n, t, f)
 * - Special values: read-only, tmpfs
 */
class ConfigParseProtectHomeOptionValue : SimpleGrammarOptionValues(
    "config_parse_protect_home",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal(
            // Boolean values
            "1", "yes", "y", "true", "t", "on",
            "0", "no", "n", "false", "f", "off",
            // Special values
            "read-only", "tmpfs"
        ),
        EOF()
    )
)
