package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Swap.Nice
 * C Function: config_parse_exec_nice(0)
 * Used by Options: Swap.Nice
 * 
 * Validates nice level (scheduling priority) values between -20 (highest priority) 
 * and 19 (lowest priority).
 */
class ConfigParseExecNiceOptionValue : SimpleGrammarOptionValues(
    "config_parse_exec_nice",
    SequenceCombinator(
        // parse_nice() is safe_atoi() plus a range check, so "+12", "0x10" and "\v5" are fine.
        // https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L724-L761
        // https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L702-L716
        SafeAtoTerminal(
            signed = true,
            base = 0,
            flags = emptySet(),
            minInclusive = -20L,
            maxInclusive = 19L,
            acceptsWhitespace = true,
            acceptsMinus = true,
        ),
        EOF()
    )
)
