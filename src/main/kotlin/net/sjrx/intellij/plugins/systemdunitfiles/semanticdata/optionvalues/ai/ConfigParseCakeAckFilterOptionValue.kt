package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * [CAKE] AckFilter= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#AckFilter=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/cake.c#L680-L730   config_parse_cake_ack_filter (QDISC_KIND_CAKE)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/cake.c#L672-L678   cake_ack_filter_table + DEFINE_PRIVATE_STRING_TABLE_LOOKUP_FROM_STRING_WITH_BOOLEAN
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/string-table.c#L32-L43   string_table_lookup_from_string_with_boolean
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L23-L46   parse_boolean (case-insensitive)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L13-L37   TEST(parse_boolean) — "YES", "TRUE", "ON" accepted
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-cake.network#L25   AckFilter=aggressive
 */

/**
 * Validator for [CAKE] AckFilter=.
 *
 * C function: config_parse_cake_ack_filter (QDISC_KIND_CAKE). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * A boolean (parse_boolean() is tried first, case-insensitively: `yes` -> filter, `no` -> off) or the
 * table word `aggressive`, which is matched exactly (case-sensitive streq). The table's `no`/`yes`
 * entries are not listed as names: parse_boolean() already takes them, in any case.
 */
class ConfigParseCakeAckFilterOptionValue : SimpleGrammarOptionValues(
  "config_parse_cake_ack_filter",
  SequenceCombinator(
    PreferValidAlternativeCombinator(
      FlexibleLiteralChoiceTerminal("aggressive"),
      BOOLEAN,
    ),
    EOF(),
  ),
)
