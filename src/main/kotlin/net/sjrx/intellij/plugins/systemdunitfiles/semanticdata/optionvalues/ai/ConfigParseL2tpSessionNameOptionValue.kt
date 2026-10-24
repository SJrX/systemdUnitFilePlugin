package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.INTERFACE_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [L2TPSession] Name= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#Name=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/l2tp-tunnel.c#L727-L765   config_parse_l2tp_session_name (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L785-L830   ifname_valid_full (flags 0, via ifname_valid)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-socket-util.c#L29   TEST(ifname_valid)
 */

/**
 * Validator for `[L2TPSession] Name=` (.netdev).
 *
 * C function: config_parse_l2tp_session_name, which requires ifname_valid(): 1…15 printable ASCII
 * characters other than `:`, `/` and `%`, not entirely digits, and not `.`, `..`, `all` or
 * `default`. See [INTERFACE_NAME]. An invalid value is logged and ignored by systemd.
 *
 * There is no isempty() branch, so an empty `Name=` is a parse error in systemd (logged and ignored), not
 * a reset. The plugin never runs a grammar on an empty value, so it is not flagged here.
 */
class ConfigParseL2tpSessionNameOptionValue : SimpleGrammarOptionValues(
    "config_parse_l2tp_session_name",
    SequenceCombinator(INTERFACE_NAME, EOF())
)
