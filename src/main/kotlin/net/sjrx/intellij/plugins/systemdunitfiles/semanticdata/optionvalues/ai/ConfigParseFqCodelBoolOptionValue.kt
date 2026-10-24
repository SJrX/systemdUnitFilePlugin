package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * [FairQueueingControlledDelay] ECN= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#ECN=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/fq-codel.c#L222-L265   config_parse_fq_codel_bool (QDISC_KIND_FQ_CODEL)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L48-L64   parse_tristate_full
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L23-L46   parse_boolean (STRCASE_IN_SET: case-insensitive)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L13-L37   TEST(parse_boolean) — "YES", "TRUE", "ON" accepted
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-fq_codel.network#L18   ECN=yes
 */

/**
 * Validator for [FairQueueingControlledDelay] ECN=.
 *
 * C function: config_parse_fq_codel_bool (QDISC_KIND_FQ_CODEL). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * A boolean via parse_tristate(): any parse_boolean() spelling, case-insensitively. Empty resets to
 * "unset" (handled before the grammar).
 */
class ConfigParseFqCodelBoolOptionValue : SimpleGrammarOptionValues(
  "config_parse_fq_codel_bool",
  SequenceCombinator(BOOLEAN, EOF()),
)
