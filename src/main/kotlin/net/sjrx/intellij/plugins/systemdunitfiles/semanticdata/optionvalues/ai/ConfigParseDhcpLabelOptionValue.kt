package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [DHCPv4] Label= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Label=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp4.c#L2135-L2165  config_parse_dhcp_label (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L832-L852        address_label_valid
 */

/**
 * Validator for `[DHCPv4] Label=` (config_parse_dhcp_label → address_label_valid): 1…15 characters
 * (shorter than IFNAMSIZ), each printable ASCII 0x20…0x7E — so, unlike an interface name, a label may
 * contain `:`, `/`, `%` and inner spaces, and may be all digits.
 */
class ConfigParseDhcpLabelOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_label",
    SequenceCombinator(RegexTerminal("""[\s\S]+""", """[\x20-\x7E]{1,15}\z"""), EOF())
)
