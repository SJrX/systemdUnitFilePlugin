package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.MAC_ADDRESS
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SAFE_ABSOLUTE_PATH
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Link] WakeOnLanPassword= in a .link file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.link.html#WakeOnLanPassword=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/udev/net/link-config.c#L2050-L2093   config_parse_wol_password (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/udev/net/link-config.c#L134-L155   link_parse_wol_password → parse_ether_addr
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/ether-addr-util.c#L282-L295   parse_ether_addr (parse_hw_addr_full, ETH_ALEN)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/path-util.h#L152-L154   path_is_safe
 */

/**
 * Validator for `[Link] WakeOnLanPassword=` (.link).
 *
 * C function: config_parse_wol_password. If the value is an absolute path that path_is_safe() accepts
 * (no `..` component) it names a file to read the password from; otherwise it must be the 6-byte
 * SecureOn password written like an Ethernet address (parse_ether_addr: `00:11:22:33:44:55`,
 * `00-11-22-33-44-55` or `0011.2233.4455`). The path is taken verbatim (no specifiers, spaces are
 * ordinary characters). An invalid value is logged and ignored by systemd.
 */
class ConfigParseWolPasswordOptionValue : SimpleGrammarOptionValues(
    "config_parse_wol_password",
    AlternativeCombinator(
        SequenceCombinator(MAC_ADDRESS, EOF()),
        SequenceCombinator(SAFE_ABSOLUTE_PATH, EOF()),
    )
)
