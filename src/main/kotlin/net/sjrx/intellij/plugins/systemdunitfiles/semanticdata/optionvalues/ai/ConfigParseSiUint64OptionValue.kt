package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for Link.BitsPerSecond.
 *
 * C function: config_parse_si_uint64 in src/shared/conf-parser.c. Internally calls
 * parse_size(rvalue, 1000, &sz), so the value is an unsigned decimal optionally followed by
 * a case-sensitive SI suffix (K, M, G, T, P, E or B) — base 1000, not 1024. Negative values
 * and lowercase suffixes (e.g. "1k") are rejected. Decimal fractions (e.g. "1.5G") are
 * accepted by parse_size.
 */
class ConfigParseSiUint64OptionValue : SimpleGrammarOptionValues(
    "config_parse_si_uint64",
    SequenceCombinator(
        OptionalWhitespacePrefix(
            RegexTerminal("[0-9]+(\\.[0-9]+)?[a-zA-Z]?\\s*", "[0-9]+(\\.[0-9]+)?[KMGTPEB]?\\s*")
        ),
        EOF()
    )
)
