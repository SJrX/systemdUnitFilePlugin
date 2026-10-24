package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * [BFIFO] LimitBytes= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#LimitBytes=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/fifo.c#L103-L160   config_parse_bfifo_size (QDISC_KIND_BFIFO)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L157-L283   parse_size
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L108-L176   TEST(parse_size_iec)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L178-L246   TEST(parse_size_si)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-htb-fifo.network#L42   LimitBytes=1000000
 */

/**
 * Validator for [BFIFO] LimitBytes=.
 *
 * C function: config_parse_bfifo_size (QDISC_KIND_BFIFO). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * A byte size as parse_size(rvalue, 1024) reads it (see [parseSize]) that must not exceed UINT32_MAX:
 * above it the parser logs "Invalid 'LimitBytes=', ignoring assignment". So `4G` is rejected.
 */
class ConfigParseBfifoSizeOptionValue : SimpleGrammarOptionValues(
  "config_parse_bfifo_size",
  SequenceCombinator(parseSize(1024, maxInclusive = SIZE_UINT32_MAX), EOF()),
)
