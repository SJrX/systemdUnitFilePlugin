package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for Link.ImportProperty and Link.UnsetProperty
 * C Function: config_parse_udev_property_name(0)
 *
 * A whitespace-separated list of udev property names. A udev property name is
 * validated by udev_property_name_is_valid which delegates to env_name_is_valid:
 *   - non-empty
 *   - first character must not be a digit
 *   - all characters must be ASCII letters, digits, or underscore
 */
class ConfigParseUdevPropertyNameOptionValue : SimpleGrammarOptionValues(
    "config_parse_udev_property_name",
    SequenceCombinator(
        RegexTerminal("[A-Za-z_][A-Za-z0-9_]*", "[A-Za-z_][A-Za-z0-9_]*"),
        ZeroOrMore(
            SequenceCombinator(
                WhitespaceTerminal(),
                RegexTerminal("[A-Za-z_][A-Za-z0-9_]*", "[A-Za-z_][A-Za-z0-9_]*")
            )
        ),
        EOF()
    )
)
