package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Network] Zone= (.nspawn).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#Zone=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd-nspawn.html#--network-zone=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-settings.c#L544-L570   config_parse_network_zone (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L785-L830   ifname_valid_full (flags 0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L769-L783   ifname_valid_char
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-13-NSPAWN.nspawn.sh#L556
 * linux  https://man7.org/linux/man-pages/man7/netdevice.7.html
 */

/**
 * Validator for the .nspawn `[Network] Zone=` setting (config_parse_network_zone, ltype 0).
 *
 * The raw value is prefixed with `vz-` and the result must pass ifname_valid(): every character
 * printable ASCII (33…126) other than `:`, `/` and `%`, and the whole name at most IFNAMSIZ-1 = 15
 * bytes — leaving 12 for the zone name itself. The prefix makes the numeric / `.` / `all` / `default`
 * exclusions irrelevant. An invalid name is logged and ignored.
 */
class ConfigParseNetworkZoneOptionValue : SimpleGrammarOptionValues(
    "config_parse_network_zone",
    SequenceCombinator(
        RegexTerminal("""\S+""", """[\x21-\x7E&&[^:/%]]{1,12}"""),
        EOF(),
    )
)
