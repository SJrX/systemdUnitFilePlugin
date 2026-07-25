package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BOOLEAN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.unsignedNumber

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
 * `[Address] DuplicateAddressDetection=` — table entry config_parse_address_dad, resolved through
 * duplicate_address_detection_address_family_table (src/network/networkd-util.c).
 */
class ConfigParseAddressSectionDadOptionValue : SimpleGrammarOptionValues(
    ADDRESS, SequenceCombinator(FlexibleLiteralChoiceTerminal("none", "both", "ipv4", "ipv6"), EOF())
)

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
