package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import java.math.BigInteger
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/*
 * [QuickFairQueueingClass] MaxPacketBytes= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#MaxPacketBytes=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/qfq.c#L115-L173   config_parse_qfq_max_packet (TCLASS_KIND_QFQ)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L157-L283   parse_size
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/tc/qfq.c#L14-L15   QFQ_MIN_MAX_PACKET 512, QFQ_MAX_MAX_PACKET 1<<16
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L108-L176   TEST(parse_size_iec)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L178-L246   TEST(parse_size_si)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-qdisc-qfq.network#L17   MaxPacketBytes=16000
 */

/**
 * Validator for [QuickFairQueueingClass] MaxPacketBytes=.
 *
 * C function: config_parse_qfq_max_packet (TCLASS_KIND_QFQ). An empty assignment resets the setting and is
 * handled before this grammar runs.
 *
 * A byte size as parse_size(rvalue, 1024) reads it (IEC suffixes `K`/`M`/…, see [parseSize]) that
 * must lie in 512…65536 bytes (QFQ_MIN_MAX_PACKET…QFQ_MAX_MAX_PACKET); outside it the parser logs
 * "Invalid ..., ignoring assignment". So `64K` is accepted and `65K` or `511` are not.
 */
class ConfigParseQfqMaxPacketOptionValue : SimpleGrammarOptionValues(
  "config_parse_qfq_max_packet",
  SequenceCombinator(parseSize(1024, BigInteger.valueOf(512), BigInteger.valueOf(65_536)), EOF()),
)
