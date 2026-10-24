package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [DHCPv4] FallbackLeaseLifetimeSec= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#FallbackLeaseLifetimeSec=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp4.c#L2098-L2133  config_parse_dhcp_fallback_lease_lifetime (0)
 */

/**
 * Validator for `[DHCPv4] FallbackLeaseLifetimeSec=`. Despite the `Sec` suffix this is not a time span:
 * the parser accepts only `forever` or `infinity` (STR_IN_SET, case-sensitive), as the man page says.
 */
class ConfigParseDhcpFallbackLeaseLifetimeOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_fallback_lease_lifetime",
    SequenceCombinator(FlexibleLiteralChoiceTerminal("forever", "infinity"), EOF())
)
