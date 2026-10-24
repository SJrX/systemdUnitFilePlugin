package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.siSize

/*
 * [BatmanAdvanced] GatewayBandwidthDown= / GatewayBandwidthUp= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#GatewayBandwidthDown=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/batadv.c#L53-L89   config_parse_badadv_bandwidth (0)
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
 * The parser only *warns* when the result divided by 100 kbit exceeds UINT32_MAX and still stores it,
 * so no magnitude bound is applied here. An invalid value is logged and ignored by systemd.
 *
 * There is no isempty() branch, so an empty `GatewayBandwidthDown=` or `GatewayBandwidthUp=` is a parse error in systemd (logged and ignored), not
 * a reset. The plugin never runs a grammar on an empty value, so it is not flagged here.
 */
class ConfigParseBadadvBandwidthOptionValue : SimpleGrammarOptionValues(
    "config_parse_badadv_bandwidth",
    SequenceCombinator(siSize(), EOF())
)
