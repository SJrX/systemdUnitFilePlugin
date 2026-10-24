package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Address] Label= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Label=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-address.c#L2298-L2363   config_parse_address_section (ADDRESS_LABEL)
 *        https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-address.c#L2174-L2200   config_parse_address_label
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L832-L852           address_label_valid
 */

/**
 * Validator for `[Address] Label=` (.network) — the ADDRESS_LABEL slot, whose table entry is
 * config_parse_address_label → address_label_valid().
 *
 * One to 15 characters (shorter than IFNAMSIZ), each a byte in 32…126 — printable ASCII, space
 * included. A longer or non-ASCII label is ignored with "Interface label is too long or invalid".
 */
class ConfigParseAddressLabelOptionValue : SimpleGrammarOptionValues(
    "config_parse_address_section",
    SequenceCombinator(RegexTerminal("""[\s\S]+""", """[\x20-\x7E]{1,15}"""), EOF())
)
