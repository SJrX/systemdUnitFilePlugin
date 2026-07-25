package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV6_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

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
 * One divergence: for the static mode systemd also rejects an address whose low 64 bits are zero
 * (`static:::`), since only those bits are used. That is a property of the parsed value rather than
 * of its shape, so this grammar accepts it.
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
            SequenceCombinator(LiteralChoiceTerminal("static:"), IPV6_ADDR),
            IPV6_ADDR,
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
