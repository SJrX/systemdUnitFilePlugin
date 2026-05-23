package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for the [SR-IOV] uint32 options: VirtualFunction=, VLANId=, QualityOfService=.
 *
 * C function: config_parse_sr_iov_uint32 in src/shared/netif-sriov.c. After branching on the
 * lvalue it calls safe_atou32. Tighter per-lvalue bounds exist (VLANId 1..4095,
 * VirtualFunction < INT_MAX) but they can't be expressed in this validator since it has no
 * access to the lvalue at evaluation time. The conservative shape is "any uint32".
 */
class ConfigParseSrIovUint32OptionValue : SimpleGrammarOptionValues(
    "config_parse_sr_iov_uint32",
    SequenceCombinator(
        IntegerTerminal(0, 4_294_967_296L),
        EOF()
    )
)
