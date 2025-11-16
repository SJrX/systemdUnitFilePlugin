package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Files.PrivateUsersOwnership
 * C Function: config_parse_userns_ownership(0)
 * Used by Options: Files.PrivateUsersOwnership
 * 
 * Validates user namespace ownership modes:
 * - off: Disable ownership adjustment
 * - chown: Change ownership of files
 * - map: Map ownership via user namespaces
 * 
 * Also accepts boolean values (yes/no/true/false/on/off/1/0) due to
 * DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN macro.
 */
class ConfigParseUsernsOwnershipOptionValue : SimpleGrammarOptionValues(
    "config_parse_userns_ownership",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal(
            "off",
            "chown",
            "map",
            "yes",
            "no",
            "true",
            "false",
            "on",
            "1",
            "0",
            "y",
            "n",
            "t",
            "f"
        ),
        EOF()
    )
)
