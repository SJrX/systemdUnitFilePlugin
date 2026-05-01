package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for config_parse_string with CONFIG_PARSE_STRING_SAFE.
 * Used by Options: Tun.User, Tun.Group, Tap.User, Tap.Group, Exec.User (nspawn),
 *                  DHCPv4.VendorClassIdentifier, *.NetLabel, Manager.RuntimeWatchdogPreGovernor, ...
 *
 * The C path is config_parse_string -> string_is_safe(rvalue, STRING_ALLOW_GLOBS):
 *   - rejects control characters (0..31, 127)
 *   - rejects backslash
 *   - rejects quotes (" and ')
 *   - allows globs (*, ?, [)
 *   - empty resets and is always valid (handled outside the grammar)
 */
class ConfigParseStringOptionValue : SimpleGrammarOptionValues(
    "config_parse_string",
    SequenceCombinator(
        RegexTerminal("[^\\x00-\\x1F\\x7F\"'\\\\]+", "[^\\x00-\\x1F\\x7F\"'\\\\]+"),
        EOF()
    )
)
