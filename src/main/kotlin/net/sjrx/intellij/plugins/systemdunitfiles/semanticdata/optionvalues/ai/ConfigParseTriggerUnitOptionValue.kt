package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/*
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L2116-L2165   config_parse_trigger_unit
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/unit-printf.c#L147-L177   unit_name_printf
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/unit-name.c#L36-L82   unit_name_is_valid
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/unit.c#L282-L283   unit_add_name (no instances of untemplatable types)
 */

/**
 * Validator for [Path] Unit= and [Timer] Unit=.
 * C function: config_parse_trigger_unit (src/core/load-fragment.c)
 *
 * The C path expands specifiers with unit_name_printf, requires the result to be a valid unit name
 * (unit_name_to_type), and adds the trigger dependency, which loads the unit and so refuses an instance
 * or template of a type that cannot be templated (only service, socket, target, timer and path can).
 * A template such as `foo@.service` gets this unit's instance filled in.
 *
 * A value that uses a unit_name_printf specifier (`brltty@-etc-brltty_%i.conf.service`) is accepted as
 * long as every specifier is known (`%i %j %n %N %p`, COMMON_SYSTEM and COMMON_CREDS), because what it
 * expands to depends on this unit's own name. That a unit cannot trigger itself is not modelled.
 */
class ConfigParseTriggerUnitOptionValue : SimpleGrammarOptionValues(
    "config_parse_trigger_unit",
    SequenceCombinator(
        RegexTerminal(
            "[\\s\\S]+",
            // a literal unit name: shorter than UNIT_NAME_MAX (256), not starting with '@', VALID_CHARS_WITH_AT, a known type
            "(?=[\\s\\S]{1,255}$)(?!@)(?:" +
                "[A-Za-z0-9:_.\\\\-]+\\.(?:service|socket|target|device|mount|automount|timer|swap|path|slice|scope)" +
                "|[A-Za-z0-9:_.\\\\@-]*@[A-Za-z0-9:_.\\\\@-]*\\.(?:service|socket|target|timer|path)" +
            ")$" +
            // or anything that uses at least one known specifier and no unknown one
            "|(?=(?:[^%]|%%|%(?![A-Za-z0-9%]))*%[ijnNpaAbBHlqmMovwWgGuU])(?:[^%]|%%|%[ijnNpaAbBHlqmMovwWgGuU]|%(?![A-Za-z0-9%]))*$"
        ),
        EOF()
    )
)
