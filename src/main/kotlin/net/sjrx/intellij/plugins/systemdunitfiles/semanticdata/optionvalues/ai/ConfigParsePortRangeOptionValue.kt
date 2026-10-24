package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.HYPHEN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

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
 * `low-high` range, each bound a safe_atou() number (hex, octal and `0b`/`0o` work) in 1…65535.
 * parse_range() cuts the whole value at the first `-`, so the lower bound never sees a minus (`-0` is an empty
 * word) and the upper bound is the rest. Whitespace inside the value reaches safe_atou(), which skips it before
 * the number, so `1000- 2000`, `0b 1-5` and `1-0b 1` are legal (but `1000 -2000` is not: the lower word keeps
 * its trailing space).
 * A range with `high < low` is refused by systemd after parsing; that compares numbers and is not
 * modelled. An invalid value is logged and ignored by systemd. There is no isempty() branch, so `PortRange=`
 * is a parse error in systemd rather than a reset; the plugin never runs a grammar on an empty value, so it
 * is not flagged here.
 */
class ConfigParsePortRangeOptionValue : SimpleGrammarOptionValues(
    "config_parse_port_range",
    SequenceCombinator(
        SafeAtoTerminal(
            signed = false,
            base = 0,
            flags = emptySet(),
            minInclusive = 1L,
            maxInclusive = 65_535L,
            acceptsWhitespace = true,
            acceptsMinus = false,
        ),
        ZeroOrOne(
            SequenceCombinator(
                HYPHEN,
                SafeAtoTerminal(
                    signed = false,
                    base = 0,
                    flags = emptySet(),
                    minInclusive = 1L,
                    maxInclusive = 65_535L,
                    acceptsWhitespace = true,
                    acceptsMinus = true,
                ),
            ),
        ),
        EOF(),
    )
)
