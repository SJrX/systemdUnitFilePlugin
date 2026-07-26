package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BOOLEAN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.deprecatedBoolean
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.unsignedNumber

/*
 * The remaining [Address] and [NextHop] table slots of a .network file.
 *
 * man     https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BAddress%5D%20Section%20Options
 * parsers https://github.com/systemd/systemd/blob/a8e93919c3/src/network/networkd-address.c  config_parse_address_dad, config_parse_uint32_flag
 *         https://github.com/systemd/systemd/blob/a8e93919c3/src/network/networkd-nexthop.c  config_parse_nexthop_family, config_parse_nexthop_group
 *
 * These slots previously shared one grammar per section — a uint32 for every [Address] entry and a
 * boolean for every [NextHop] entry — which flagged legitimate values such as AddPrefixRoute=no,
 * DuplicateAddressDetection=ipv4, Id=20 and Family=ipv4.
 */

/*
 * The remaining `[Address]` and `[NextHop]` table slots (#509).
 *
 * These previously shared one grammar per section — a uint32 for every `[Address]` slot and a boolean
 * for every `[NextHop]` slot — even though the ConfigSectionParser tables give each slot its own
 * parser. That flagged legitimate values: `AddPrefixRoute=no`, `HomeAddress=yes`,
 * `DuplicateAddressDetection=ipv4`, `[NextHop] Id=20` and `Family=ipv4` were all reported as invalid.
 * Each slot now gets the grammar its own table entry calls for.
 */

private const val ADDRESS = "config_parse_address_section"
private const val NEXTHOP = "config_parse_nexthop_section"

/**
 * `[Address] HomeAddress=`, `ManageTemporaryAddress=`, `PrefixRoute=`, `AddPrefixRoute=` and
 * `AutoJoin=` — table entries config_parse_uint32_flag / config_parse_uint32_invert_flag. Both read
 * the value with parse_boolean() and differ only in which IFA_F_* bit they set, and in which
 * direction.
 */
class ConfigParseAddressSectionFlagOptionValue : SimpleGrammarOptionValues(
    ADDRESS, SequenceCombinator(BOOLEAN, EOF())
)

/**
 * `[Address] DuplicateAddressDetection=` — table entry config_parse_address_dad.
 *
 * The man page documents only the four family names, but the parser tries parse_boolean() *first* and
 * accepts a boolean with nothing worse than a warning:
 *
 * ```c
 * r = parse_boolean(rvalue);
 * if (r >= 0) {
 *         log_syntax(unit, LOG_WARNING, filename, line, 0,
 *                    "For historical reasons, %s=%s means %s=%s. "
 *                    "Please use 'both', 'ipv4', 'ipv6' or 'none' instead.", …);
 * ```
 *
 * So the booleans are accepted here too, but marked deprecated so the editor repeats systemd's advice
 * instead of reporting an error on a line networkd honours. This is a case where the C source and the
 * man page disagree and the source wins.
 */
class ConfigParseAddressSectionDadOptionValue : SimpleGrammarOptionValues(
    ADDRESS, SequenceCombinator(DAD, EOF())
) {
    private companion object {
        private const val HISTORICAL =
            "For historical reasons a boolean here means the opposite of what it looks like: " +
            "yes means none and no means both. Please use 'both', 'ipv4', 'ipv6' or 'none' instead."

        // The family names come first: a boolean terminal matches a prefix of the value, so it would
        // otherwise take the leading "no" out of "none" and strand "ne".
        val DAD = AlternativeCombinator(
            FlexibleLiteralChoiceTerminal("none", "both", "ipv4", "ipv6"),
            deprecatedBoolean(HISTORICAL),
        )
    }
}

/** `[NextHop] Id=` — table entry config_parse_uint32. */
class ConfigParseNextHopIdOptionValue : SimpleGrammarOptionValues(
    NEXTHOP, SequenceCombinator(unsignedNumber(4_294_967_296L), EOF())
)

/**
 * `[NextHop] Family=` — table entry config_parse_nexthop_family, resolved through
 * nexthop_address_family_table, which unlike its siblings offers only the two concrete families.
 */
class ConfigParseNextHopFamilyOptionValue : SimpleGrammarOptionValues(
    NEXTHOP, SequenceCombinator(FlexibleLiteralChoiceTerminal("ipv4", "ipv6"), EOF())
)

/**
 * `[NextHop] Group=` — table entry config_parse_nexthop_group: a whitespace-separated list of
 * `id[:weight]`, where the id is a uint32 and the weight, when present, is 1…256.
 */
class ConfigParseNextHopGroupOptionValue : SimpleGrammarOptionValues(
    NEXTHOP,
    SequenceCombinator(
        GROUP_MEMBER,
        ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), GROUP_MEMBER)),
        EOF()
    )
) {
    private companion object {
        val GROUP_MEMBER = SequenceCombinator(
            unsignedNumber(4_294_967_296L),
            ZeroOrOne(SequenceCombinator(LiteralChoiceTerminal(":"), unsignedNumber(257, minInclusive = 1)))
        )
    }
}
