package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for Slice= (the unit's enclosing slice).
 *
 * C function: config_parse_unit_slice in src/core/load-fragment.c. It calls unit_name_printf
 * (which expands specifiers like %p/%n) and then manager_load_unit, which validates that the
 * resolved name is a real slice unit. Pre-expansion, the value must be a single token; if it
 * contains specifiers it can produce anything, otherwise it must already look like a slice
 * (ends in ".slice"). Whitespace-separated lists are not accepted.
 */
class ConfigParseUnitSliceOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_slice",
    SequenceCombinator(
        AlternativeCombinator(
            // Literal slice unit name: no whitespace, must end in .slice
            RegexTerminal("\\S+", "[^\\s%]+\\.slice"),
            // Contains a specifier — expansion can produce any unit name
            RegexTerminal("\\S+", "\\S*%\\S+|\\S+%\\S*")
        ),
        EOF()
    )
)
