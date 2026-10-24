package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for CAKE.UseRawPacketSize (and AutoRateIngress=, NAT=, Wash=, SplitGSO=)
 * C Function: config_parse_cake_tristate(QDISC_KIND_CAKE), src/network/tc/cake.c
 *
 * Reads the value with parse_tristate() -> parse_tristate_full() -> parse_boolean(), so it is a
 * plain boolean in any casing (the empty value is handled by the framework).
 */
class ConfigParseCakeTristateOptionValue : SimpleGrammarOptionValues(
    "config_parse_cake_tristate",
    SequenceCombinator(
        BOOLEAN,
        EOF()
    )
)
