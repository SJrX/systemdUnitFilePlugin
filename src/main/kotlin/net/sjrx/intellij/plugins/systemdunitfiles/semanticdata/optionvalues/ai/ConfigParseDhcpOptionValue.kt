package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Network.DHCP
 * C Function: config_parse_dhcp(0), src/network/networkd-dhcp-common.c
 *
 * address_family_from_string first — DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN over address_family_table
 * (no, yes, ipv4, ipv6) in src/network/networkd-util.c, so any boolean in any casing — then the
 * case-sensitive dhcp_deprecated_address_family_table (none, both, v4, v6), which is accepted with a
 * "DHCP=%s is deprecated" warning.
 */
class ConfigParseDhcpOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp",
    SequenceCombinator(
        // One names terminal (not two alternatives) so `none` is tried before `no` — the choices are
        // matched longest first.
        namesOrBoolean(
            FlexibleLiteralChoiceTerminal("no", "yes", "ipv4", "ipv6", "none", "both", "v4", "v6").deprecating(
                mapOf(
                    "none" to "DHCP=none is deprecated, please use DHCP=no instead.",
                    "both" to "DHCP=both is deprecated, please use DHCP=yes instead.",
                    "v4" to "DHCP=v4 is deprecated, please use DHCP=ipv4 instead.",
                    "v6" to "DHCP=v6 is deprecated, please use DHCP=ipv6 instead.",
                )
            )
        ),
        EOF()
    )
)
