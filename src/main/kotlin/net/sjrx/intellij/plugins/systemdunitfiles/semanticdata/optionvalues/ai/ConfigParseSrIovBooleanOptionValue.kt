package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for the [SR-IOV] boolean options: MACSpoofCheck=, QueryReceiveSideScaling=, Trust=.
 *
 * C function: config_parse_sr_iov_boolean in src/shared/netif-sriov.c. After branching on the
 * lvalue it calls parse_boolean(rvalue), so the accepted syntax is exactly the values handled
 * by the existing BOOLEAN grammar terminal.
 */
class ConfigParseSrIovBooleanOptionValue : SimpleGrammarOptionValues(
    "config_parse_sr_iov_boolean",
    SequenceCombinator(
        BOOLEAN,
        EOF()
    )
)
