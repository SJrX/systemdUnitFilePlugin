package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoNumberTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [DHCPPrefixDelegation] SubnetId= (and the old [DHCPv6PrefixDelegation] SubnetId=) (.network).
 *
 * Note: the plugin reports the deprecated [DHCPv6PrefixDelegation] section as not allowed in .network files, so this
 * validator never actually runs for keys there; they are listed only because the gperf shares the parser.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#SubnetId=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-prefix-delegation.c#L1428-L1470  config_parse_dhcp_pd_subnet_id (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L522-L558                          safe_atollu_full (safe_atoux64: explicit base 16)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-dhcp6pd-upstream.network#L16
 */

/**
 * Validator for `SubnetId=` (config_parse_dhcp_pd_subnet_id): `auto`, or a *hexadecimal* number (always
 * base 16 — `SubnetId=10` means 0x10 — with an optional `0x`) no larger than INT64_MAX, i.e.
 * 0x7fffffffffffffff.
 *
 * `auto` comes first, but `a`, `add` etc. are hex numbers too; each branch ends in EOF so the classic
 * engine can't strand one in the other.
 */
class ConfigParseDhcpPdSubnetIdOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_pd_subnet_id",
    AlternativeCombinator(
        SequenceCombinator(FlexibleLiteralChoiceTerminal("auto"), EOF()),
        SequenceCombinator(SafeAtoNumberTerminal(0, Long.MAX_VALUE, hexOnly = true), EOF()),
    )
)
