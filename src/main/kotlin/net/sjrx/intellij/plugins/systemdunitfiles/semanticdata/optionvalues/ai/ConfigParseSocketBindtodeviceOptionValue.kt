package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.INTERFACE_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.UNIT_SPECIFIER_LETTERS

/*
 * BindToDevice= ([Socket]).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.socket.html#BindToDevice=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L1097-L1134   config_parse_socket_bindtodevice (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/unit-printf.c#L249   unit_full_printf
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L785-L830   ifname_valid_full (flags 0: < IFNAMSIZ, not numeric, not ./../all/default)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L769   ifname_valid_char (33..126 except ':' '/' '%')
 * linux  https://man7.org/linux/man-pages/man7/socket.7.html   (SO_BINDTODEVICE)
 * linux  https://man7.org/linux/man-pages/man7/netdevice.7.html   (IFNAMSIZ)
 */

// Characters ifname_valid_char accepts.
private const val IFNAME_CHAR = """[\x21-\x7E&&[^:/%]]"""

// A specifier that can appear inside an interface name. `%%` is left out on purpose: it expands to a
// literal `%`, which ifname_valid_char refuses.
private val IFNAME_SPECIFIER = "%[${UNIT_SPECIFIER_LETTERS.replace("%", "")}]"

/**
 * One interface name, possibly built from specifiers. Without a `%` it must satisfy the same rules as
 * [INTERFACE_NAME] (ifname_valid with no flags: 1–15 characters from ifname_valid_char, not all digits,
 * not `.`, `..`, `all` or `default`). With specifiers the expanded length and content are unknown, so
 * only the literal characters are checked and every `%x` must be a known specifier.
 */
private val IFNAME_WITH_SPECIFIERS = RegexTerminal(
    """\S+""",
    """(?:${INTERFACE_NAME.semanticMatch.pattern})(?!\S)|(?=\S*%)(?:$IFNAME_CHAR|$IFNAME_SPECIFIER)+(?!\S)"""
)

/**
 * Validator for `BindToDevice=`.
 *
 * `*` (like the empty value) clears the setting. Anything else is run through unit_full_printf() and
 * must then be a valid interface name per ifname_valid() with no flags — so alternative names longer
 * than 15 characters are refused, unlike BindNetworkInterface=. An invalid name is "ignoring", so it is
 * flagged. Specifiers are allowed (`BindToDevice=%i` in a template socket is a real pattern).
 *
 * Deliberately lenient: a name containing specifiers is not length-checked, and numeric spellings that
 * parse_ifindex() would take as an index (`0x5`, `+5`) are not refused — only plain all-digit names are.
 *
 * Covers: Socket.BindToDevice.
 */
class ConfigParseSocketBindtodeviceOptionValue : SimpleGrammarOptionValues(
    "config_parse_socket_bindtodevice",
    AlternativeCombinator(
        SequenceCombinator(LiteralChoiceTerminal("*"), EOF()),
        SequenceCombinator(IFNAME_WITH_SPECIFIERS, EOF()),
    )
)
