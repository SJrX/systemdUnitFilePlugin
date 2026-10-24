package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ALTERNATIVE_INTERFACE_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore

/*
 * [Link] AlternativeName= (.link) and [HSR] Ports= (.netdev).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.link.html#AlternativeName=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#Ports=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L1651-L1702   config_parse_ifnames (IFNAME_VALID_ALTERNATIVE)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L785-L830   ifname_valid_full
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-hsr.netdev#L7   Ports=test1 dummy98
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-socket-util.c#L29   TEST(ifname_valid)
 */

/**
 * Validator for `[Link] AlternativeName=` (.link) and `[HSR] Ports=` (.netdev).
 *
 * C function: config_parse_ifnames with ltype IFNAME_VALID_ALTERNATIVE: a whitespace-separated list
 * (extract_first_word, no unquoting) of interface names, each checked by ifname_valid_full with
 * IFNAME_VALID_ALTERNATIVE — up to 127 printable ASCII characters other than `:`, `/` and `%`, not
 * entirely digits, and not `.`, `..`, `all` or `default`. See [ALTERNATIVE_INTERFACE_NAME]. An
 * invalid name is logged and skipped; an empty assignment clears the list.
 */
class ConfigParseIfnamesOptionValue : SimpleGrammarOptionValues(
    "config_parse_ifnames",
    SequenceCombinator(
        ALTERNATIVE_INTERFACE_NAME,
        ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), ALTERNATIVE_INTERFACE_NAME)),
        EOF()
    )
)
