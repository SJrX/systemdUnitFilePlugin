package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L3714-L3754   config_parse_unit_slice
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/manager.c#L2748-L2755   manager_load_unit_prepare (valid plain or instance name)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/unit.c#L282-L283   unit_add_name (slices cannot be instances)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/unit.c#L3483-L3484   unit_set_slice (must be a slice)
 */

/**
 * Validator for Slice= (the unit's enclosing slice).
 *
 * C function: config_parse_unit_slice in src/core/load-fragment.c. It calls unit_name_printf
 * (which expands specifiers like %p/%n) and then manager_load_unit, which needs a valid unit name, and
 * unit_set_slice, which needs a slice. Slices cannot be templated, so `some@instance.slice` and
 * `foo@.slice` are refused. Whitespace-separated lists are not accepted.
 *
 * A value that uses a unit_name_printf specifier (`%i %j %n %N %p`, COMMON_SYSTEM and COMMON_CREDS) can
 * produce anything, so it is accepted as long as every specifier is known.
 */
class ConfigParseUnitSliceOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_slice",
    SequenceCombinator(
        AlternativeCombinator(
            // Literal slice unit name: VALID_CHARS (no '@'), shorter than UNIT_NAME_MAX, ends in .slice
            RegexTerminal("\\S+", "(?=\\S{1,255}$)[A-Za-z0-9:_.\\\\-]+\\.slice"),
            // Contains a known specifier and no unknown one: expansion can produce any unit name
            RegexTerminal(
                "\\S+",
                "(?=(?:[^%\\s]|%%|%(?![A-Za-z0-9%]))*%[ijnNpaAbBHlqmMovwWgGuU])(?:[^%\\s]|%%|%[ijnNpaAbBHlqmMovwWgGuU]|%(?![A-Za-z0-9%]))*"
            )
        ),
        EOF()
    )
)
