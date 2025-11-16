package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Swap.MountFlags
 * C Function: config_parse_exec_mount_propagation_flag(0)
 * Used by Options: Swap.MountFlags
 * 
 * Validates mount propagation settings: shared, slave, or private.
 * These control whether file system mount points in the file system namespaces 
 * will receive or propagate mounts and unmounts from other file system namespaces.
 */
class ConfigParseExecMountPropagationFlagOptionValue : SimpleGrammarOptionValues(
    "config_parse_exec_mount_propagation_flag",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("shared", "slave", "private"),
        EOF()
    )
)
