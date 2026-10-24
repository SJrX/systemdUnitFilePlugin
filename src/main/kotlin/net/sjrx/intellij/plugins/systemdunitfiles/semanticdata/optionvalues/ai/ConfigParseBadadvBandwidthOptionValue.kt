package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NetdevLinkCheckedSpan
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.netdevLinkParseSizeSi
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.siSize
import java.math.BigInteger

/*
 * [BatmanAdvanced] GatewayBandwidthDown= / GatewayBandwidthUp= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#GatewayBandwidthDown=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/batadv.c#L53-L89   config_parse_badadv_bandwidth (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/batadv.c#L81-L84   (the 100 kbit uint32_t range warning)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L157-L283   parse_size (base 1000)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L178   TEST(parse_size_si)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-batadv.netdev#L14-L15
 */

/**
 * Validator for `[BatmanAdvanced] GatewayBandwidthDown=` and `GatewayBandwidthUp=` (.netdev), plus the
 * misspelled `GatewayBandwithDown=` / `GatewayBandwithUp=` that systemd still accepts as aliases.
 *
 * C function: config_parse_badadv_bandwidth, which hands the whole value to parse_size(rvalue, 1000, …):
 * a number with an optional case-sensitive SI suffix (`K`, `M`, `G`, `T`, `P`, `E`, `B`), a
 * fraction allowed, and several elements with strictly descending suffixes (`1G 500M`) summed. See
 * [siSize].
 *
 * The total is stored in units of 100 kbit in a uint32_t, so a total of 4294967296 × 100000
 * (429496729600000, e.g. `12P` or `3E 2P`) or more is flagged. systemd logs that case ("is outside of
 * 0...429496729500000 range") and then stores the total truncated to 32 bits, which is never what was
 * meant, so it is treated as invalid here. Totals that overflow parse_size itself (`20E`) are refused
 * outright. Confirmed against systemd's parser built at 0f0682b773: `429496729599999` is silent,
 * `429496729600000` warns. Any other invalid value is logged and ignored by systemd.
 *
 * There is no isempty() branch, so an empty `GatewayBandwidthDown=` or `GatewayBandwidthUp=` is a parse error in systemd (logged and ignored), not
 * a reset. The plugin never runs a grammar on an empty value, so it is not flagged here.
 */
class ConfigParseBadadvBandwidthOptionValue : SimpleGrammarOptionValues(
    "config_parse_badadv_bandwidth",
    SequenceCombinator(
        NetdevLinkCheckedSpan(siSize(), "total < 429496729600000") { text ->
            netdevLinkParseSizeSi(text)?.let { it < BATADV_BANDWIDTH_LIMIT } == true
        },
        EOF()
    )
)

/** `k/1000/100 > UINT32_MAX` (batadv.c#L81) first holds at this total. */
private val BATADV_BANDWIDTH_LIMIT: BigInteger = BigInteger.valueOf(4_294_967_296L) * BigInteger.valueOf(100_000L)
