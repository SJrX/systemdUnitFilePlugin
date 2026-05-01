package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for {Service,Socket,Mount,Swap,Slice,Scope}.DeviceAllow.
 * C Function: config_parse_device_allow(0)
 *
 * Per valid_device_allow_pattern + cgroup_device_permissions_from_string:
 *   - device specifier is either a /dev/... path or a block-/char- prefixed device class
 *   - optional whitespace + permissions, where permissions is a single token of [rwm]+
 */
class ConfigParseDeviceAllowOptionValue : SimpleGrammarOptionValues(
    "config_parse_device_allow",
    SequenceCombinator(
        AlternativeCombinator(
            RegexTerminal("(block-|char-)\\S+", "(block-|char-)\\S+"),
            RegexTerminal("/dev/\\S+", "/dev/\\S+")
        ),
        ZeroOrOne(
            SequenceCombinator(
                WhitespaceTerminal(),
                RegexTerminal("[rwm]+", "[rwm]+")
            )
        ),
        EOF()
    )
)
