package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * [GenericRandomEarlyDetection] GenericRIO= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#GenericRIO=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/gred.c#L134-L177   config_parse_gred_bool (QDISC_KIND_GRED)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L48-L64   parse_tristate_full
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L23-L46   parse_boolean (STRCASE_IN_SET: case-insensitive)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L13-L37   TEST(parse_boolean) — "YES", "TRUE", "ON" accepted
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-gred.network#L14   GenericRIO=yes
 */

/**
 * Validator for [GenericRandomEarlyDetection] GenericRIO=.
 *
 * C function: config_parse_gred_bool (QDISC_KIND_GRED). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * A boolean via parse_tristate(): any parse_boolean() spelling (`yes`/`no`/`1`/`0`/`true`/`false`/
 * `on`/`off`/`y`/`n`/`t`/`f`), case-insensitively. Empty resets to "unset" (handled before the grammar).
 */
class ConfigParseGredBoolOptionValue : SimpleGrammarOptionValues(
  "config_parse_gred_bool",
  SequenceCombinator(QDISC_BOOLEAN, EOF()),
)
