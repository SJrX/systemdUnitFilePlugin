package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for generic signed-integer options parsed by config_parse_int.
 *
 * C function: defined via DEFINE_PARSER(int, int, safe_atoi) in src/shared/conf-parser.c.
 * Accepts a signed 32-bit integer.
 */
class ConfigParseIntOptionValue : SimpleGrammarOptionValues(
    "config_parse_int",
    SequenceCombinator(
        IntegerTerminal(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong() + 1L),
        EOF()
    )
)
