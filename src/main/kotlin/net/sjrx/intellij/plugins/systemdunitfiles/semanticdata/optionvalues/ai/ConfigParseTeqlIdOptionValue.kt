package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * [TrivialLinkEqualizer] Id= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Id=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/teql.c#L44-L100   config_parse_teql_id (QDISC_KIND_TEQL)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478   safe_atou_full
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L403-L427   mangle_base
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L489   safe_atou16("+12345") accepted
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L466-L470   0o6 / 0B101 prefixes (safe_atolli, same mangle_base)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-teql.network#L12   Id=1
 */

/**
 * Validator for [TrivialLinkEqualizer] Id=.
 *
 * C function: config_parse_teql_id (QDISC_KIND_TEQL). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * An unsigned integer read with safe_atou (base 0) that must not exceed INT_MAX: above it the parser
 * logs "'Id=' is too large, ignoring assignment" (and, at 0f0682b773, still stores it — the qdisc kind
 * then becomes e.g. "teql4294967295"), so 0…2147483647 is accepted.
 */
class ConfigParseTeqlIdOptionValue : SimpleGrammarOptionValues(
  "config_parse_teql_id",
  SequenceCombinator(safeAtou(2_147_483_648L), EOF()),
)
