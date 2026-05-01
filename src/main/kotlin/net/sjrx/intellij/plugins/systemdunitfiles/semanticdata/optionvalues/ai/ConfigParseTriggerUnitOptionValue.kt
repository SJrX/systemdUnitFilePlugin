package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for [Path] Unit= and [Timer] Unit=.
 * C function: config_parse_trigger_unit (src/core/load-fragment.c)
 *
 * The C path resolves specifiers in rvalue, then requires the result to be a
 * valid systemd unit name (unit_name_to_type must succeed). A unit name is
 * <name>.<type> where the body characters are letters, digits, '-', '_', '@',
 * ':', and '.', and <type> is one of the recognised unit type extensions.
 *
 * Used by .path (Path.Unit) and .timer (Timer.Unit) units.
 */
class ConfigParseTriggerUnitOptionValue : SimpleGrammarOptionValues(
    "config_parse_trigger_unit",
    SequenceCombinator(
        RegexTerminal(
            "[A-Za-z0-9_:.@-]+\\.(service|socket|target|device|mount|automount|timer|swap|path|slice|scope)",
            "[A-Za-z0-9_:.@-]+\\.(service|socket|target|device|mount|automount|timer|swap|path|slice|scope)"
        ),
        EOF()
    )
)
