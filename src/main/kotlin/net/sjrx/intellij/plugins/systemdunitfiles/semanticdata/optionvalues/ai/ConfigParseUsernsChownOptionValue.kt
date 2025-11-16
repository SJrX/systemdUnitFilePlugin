package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Files.PrivateUsersChown
 * C Function: config_parse_userns_chown(0)
 * Used by Options: Files.PrivateUsersChown
 * 
 * This is a deprecated option that has been renamed to PrivateUsersOwnership.
 * It accepts boolean values and maps them to user namespace ownership modes:
 * - true/yes/on/1/y/t → USER_NAMESPACE_OWNERSHIP_CHOWN
 * - false/no/off/0/n/f → USER_NAMESPACE_OWNERSHIP_OFF
 */
class ConfigParseUsernsChownOptionValue : SimpleGrammarOptionValues(
    "config_parse_userns_chown",
    SequenceCombinator(
        BOOLEAN,
        EOF()
    )
)
