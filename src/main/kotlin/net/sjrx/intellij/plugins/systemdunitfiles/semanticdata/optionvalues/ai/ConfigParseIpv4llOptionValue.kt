package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BOOLEAN_ANY_CASE
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Network] IPv4LL= (.network; deprecated in favour of LinkLocalAddressing=).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#LinkLocalAddressing=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-ipv4ll.c#L348-L387  config_parse_ipv4ll (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L23-L46           parse_boolean
 */

/**
 * Validator for the deprecated `[Network] IPv4LL=`: a boolean (parse_boolean, case-insensitive). systemd
 * logs a deprecation warning for every assignment; the value itself is still applied.
 */
class ConfigParseIpv4llOptionValue : SimpleGrammarOptionValues(
    "config_parse_ipv4ll",
    SequenceCombinator(BOOLEAN_ANY_CASE, EOF())
)
