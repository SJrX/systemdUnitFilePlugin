package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Tunnel] IPv6FlowLabel= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#IPv6FlowLabel=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/tunnel.c#L907-L942   config_parse_ipv6_flowlabel (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L2151-L2182   config_parse_unsigned_bounded → safe_atou_bounded
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478   safe_atou_full (base 0)
 */

/**
 * Validator for `[Tunnel] IPv6FlowLabel=` (.netdev).
 *
 * C function: config_parse_ipv6_flowlabel. Either `inherit` (copy the inner packet's flow label) or a
 * 20-bit flow label 0…0xFFFFF read by config_parse_uint32_bounded (safe_atou, so hex works, as the man
 * page's own range notation suggests). An out-of-range value is logged and ignored by systemd.
 */
class ConfigParseIpv6FlowlabelOptionValue : SimpleGrammarOptionValues(
    "config_parse_ipv6_flowlabel",
    SequenceCombinator(
        AlternativeCombinator(
            FlexibleLiteralChoiceTerminal("inherit"),
            SafeAtoTerminal(
                signed = false,
                base = 0,
                flags = emptySet(),
                minInclusive = 0L,
                maxInclusive = 0xfffffL,
                acceptsWhitespace = true,
                acceptsMinus = true,
            ),
        ),
        EOF()
    )
)
