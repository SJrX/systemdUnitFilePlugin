package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Link] Group= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Group=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-network.c#L1054-L1094  config_parse_link_group (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L497-L520           safe_atoi (safe_atoi32, base 0 + mangle_base)
 */

/**
 * Validator for `[Link] Group=` (config_parse_link_group): an integer 0…2147483647, read by safe_atoi32
 * — so `0x10`, `0o17`, `0b101`, `010` (octal) and `+5` are all legal, and negatives are refused by the
 * explicit `group < 0` check.
 */
class ConfigParseLinkGroupOptionValue : SimpleGrammarOptionValues(
    "config_parse_link_group",
    SequenceCombinator(
        SafeAtoTerminal(
            signed = true,
            base = 0,
            flags = emptySet(),
            minInclusive = 0L,
            maxInclusive = 2_147_483_647L,
            acceptsWhitespace = true,
            acceptsMinus = true,
        ),
        EOF(),
    )
)
