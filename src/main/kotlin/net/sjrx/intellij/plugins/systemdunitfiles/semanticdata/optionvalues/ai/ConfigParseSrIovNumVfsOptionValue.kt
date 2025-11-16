package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Link.SR-IOVVirtualFunctions
 * C Function: config_parse_sr_iov_num_vfs(0)
 * Used by Options: Link.SR-IOVVirtualFunctions
 */
class ConfigParseSrIovNumVfsOptionValue : SimpleGrammarOptionValues(
    "config_parse_sr_iov_num_vfs",
    SequenceCombinator(
        IntegerTerminal(0, 2147483648),
        EOF()
    )
)
