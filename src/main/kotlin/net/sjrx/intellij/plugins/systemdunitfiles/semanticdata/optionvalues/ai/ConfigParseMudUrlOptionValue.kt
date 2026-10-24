package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.C_ESCAPE_REGEX
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [DHCPv4] MUDURL=, [DHCPv6] MUDURL=, [LLDP] MUDURL= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#MUDURL=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-util.c#L199-L238  config_parse_mud_url (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/web-util.c#L10-L24          http_url_is_valid
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/escape.c#L104-L296          cunescape_one (via cunescape, flags 0)
 */

// After the scheme: at least one character, every one of them ASCII (ascii_is_valid), with backslashes
// only as valid C escapes (the whole value goes through cunescape first).
private val MUD_URL_REST = RegexTerminal(
    """(?:[^\\]|\\[\s\S]?)+""",
    """(?:[\x01-\x5B\x5D-\x7F]|$C_ESCAPE_REGEX)+\z""",
)

/**
 * Validator for the three `MUDURL=` settings (config_parse_mud_url).
 *
 * The value is cunescape()d and must then satisfy http_url_is_valid(): it starts with `http://` or
 * `https://` (case-sensitive, STARTSWITH_SET), has something after the scheme, and is pure ASCII. The
 * 255-byte limit on the unescaped URL is not modelled, and an escape that decodes to a non-ASCII byte
 * (`\xff`) is accepted although systemd would then reject it (both lenient).
 */
class ConfigParseMudUrlOptionValue : SimpleGrammarOptionValues(
    "config_parse_mud_url",
    SequenceCombinator(
        // An exact LiteralChoiceTerminal, not a Flexible one: the flexible shape regex would run on into the
        // host name ("http://e") and never match a choice under the list-of-successes engine.
        LiteralChoiceTerminal("http://", "https://"),
        MUD_URL_REST,
        EOF(),
    )
)
