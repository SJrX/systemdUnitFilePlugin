package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * [CAKE] FlowIsolationMode= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#FlowIsolationMode=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/cake.c#L442-L492   config_parse_cake_flow_isolation_mode (QDISC_KIND_CAKE)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/cake.c#L429-L438   cake_flow_isolation_mode_table
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/cake.c#L440   DEFINE_PRIVATE_STRING_TABLE_LOOKUP_FROM_STRING (exact, case-sensitive)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-cake.network#L18   FlowIsolationMode=dual-dst-host
 */

/**
 * Validator for [CAKE] FlowIsolationMode=.
 *
 * C function: config_parse_cake_flow_isolation_mode (QDISC_KIND_CAKE). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * One of `none`, `src-host`, `dst-host`, `hosts`, `flows`, `dual-src-host`, `dual-dst-host`, `triple`,
 * matched exactly (string_table_lookup is case-sensitive, and this table has no boolean fallback).
 */
class ConfigParseCakeFlowIsolationModeOptionValue : SimpleGrammarOptionValues(
  "config_parse_cake_flow_isolation_mode",
  SequenceCombinator(FlexibleLiteralChoiceTerminal("none", "src-host", "dst-host", "hosts", "flows", "dual-src-host", "dual-dst-host", "triple"), EOF()),
)
