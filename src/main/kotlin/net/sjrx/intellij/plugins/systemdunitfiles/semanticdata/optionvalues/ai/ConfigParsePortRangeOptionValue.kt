package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.UINT16_LIMIT
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.parseRange

/*
 * [VXLAN] PortRange= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#PortRange=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/vxlan.c#L299-L324   config_parse_port_range (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L736-L760   parse_ip_port_range (allow_zero=false)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L305-L340   parse_range
 */

/**
 * Validator for `[VXLAN] PortRange=` (.netdev).
 *
 * C function: config_parse_port_range → parse_ip_port_range(…, allow_zero = false): a single port or a
 * `low-high` range, each bound a safe_atou() number (so hex works) in 1…65535. safe_atou() skips leading
 * whitespace, so `1000- 2000` is legal (but `1000 -2000` is not: the lower word keeps its trailing space).
 * A range with `high < low` is refused by systemd after parsing; that compares numbers and is not
 * modelled. An invalid value is logged and ignored by systemd. There is no isempty() branch, so `PortRange=`
 * is a parse error in systemd rather than a reset; the plugin never runs a grammar on an empty value, so it
 * is not flagged here.
 */
class ConfigParsePortRangeOptionValue : SimpleGrammarOptionValues(
    "config_parse_port_range",
    SequenceCombinator(parseRange(UINT16_LIMIT, 1, spaceAfterDash = true), EOF())
)
