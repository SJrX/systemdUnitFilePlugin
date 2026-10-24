package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Checked
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV6_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ipLiteralBytes

/*
 * The IPv6 address-generation tokens: [Network] IPv6Token=, [IPv6AcceptRA] Token= and
 * [DHCPPrefixDelegation] / [DHCPv6PrefixDelegation] Token=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#IPv6Token=
 * parser https://github.com/systemd/systemd/blob/a8e93919c3/src/network/networkd-address-generation.c  config_parse_address_generation_type
 * static https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-address-generation.c#L523-L533   static mode: only the low 64 bits are used and must not be all zero
 * secret https://github.com/systemd/systemd/blob/a8e93919c3/src/libsystemd/sd-id128/id128-util.c       id128_from_string_nonzero
 *
 * systemd's own test/test-network/conf/25-ipv6-prefix-veth-token-prefixstable.network doubles as the
 * negative-case list for this setting.
 */

/**
 * Validator for the IPv6 address-generation tokens: `[Network] IPv6Token=`, `[IPv6AcceptRA] Token=`
 * and `[DHCPPrefixDelegation] Token=` / `[DHCPv6PrefixDelegation] Token=` (.network).
 *
 * C function: config_parse_address_generation_type in src/network/networkd-address-generation.c,
 * which recognises three modes:
 *  - `prefixstable`, optionally `:ADDRESS`, optionally `,SECRET_KEY`
 *  - `eui64`
 *  - `static:ADDRESS`, or a bare ADDRESS (the mode defaults to static)
 * ADDRESS is always parsed as IPv6, and SECRET_KEY goes through id128_from_string_nonzero, i.e. 32
 * hex digits or the dashed UUID spelling, and not all-zero.
 *
 * For the static mode systemd also rejects an address whose low 64 bits are zero (`static:::`, `::`,
 * `static:2001:db8::`: "cannot be the ANY address"), since only those bits are used; `static:::1` and
 * `::1:0:0:0` are fine (sd-check at 0f0682b773). prefixstable keeps the high 64 bits instead and has no
 * such check, so `prefixstable:::` is accepted.
 */
class ConfigParseAddressGenerationTypeOptionValue : SimpleGrammarOptionValues(
    "config_parse_address_generation_type",
    SequenceCombinator(
        AlternativeCombinator(
            SequenceCombinator(
                LiteralChoiceTerminal("prefixstable"),
                ZeroOrOne(SequenceCombinator(COLON, IPV6_ADDR)),
                ZeroOrOne(SequenceCombinator(LiteralChoiceTerminal(","), SECRET_KEY)),
            ),
            LiteralChoiceTerminal("eui64"),
            SequenceCombinator(
                LiteralChoiceTerminal("static:"),
                Checked(IPV6_ADDR) { text -> ipLiteralBytes(text)?.let { bytes -> bytes.drop(8).any { it.toInt() != 0 } } ?: true },
            ),
            Checked(IPV6_ADDR) { text -> ipLiteralBytes(text)?.let { bytes -> bytes.drop(8).any { it.toInt() != 0 } } ?: true },
        ),
        EOF()
    )
) {
    companion object {
        /** id128_from_string_nonzero: 32 hex digits or 8-4-4-4-12, and not all zeroes. */
        private val SECRET_KEY = RegexTerminal(
            """[0-9a-fA-F-]+""",
            """(?=.*[1-9a-fA-F])(?:[0-9a-fA-F]{32}|[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})"""
        )
    }
}
