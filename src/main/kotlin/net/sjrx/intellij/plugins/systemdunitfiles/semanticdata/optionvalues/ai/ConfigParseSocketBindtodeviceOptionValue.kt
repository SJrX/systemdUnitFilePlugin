package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.INTERFACE_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.MatchResult
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NoMatch
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Parse
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ParseStep
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.TerminalCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.UNIT_SPECIFIER_LETTERS
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.seccompBpfCgroupSafeAtoiValue

/*
 * BindToDevice= ([Socket]).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.socket.html#BindToDevice=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L1097-L1134   config_parse_socket_bindtodevice (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/unit-printf.c#L249   unit_full_printf
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L785-L830   ifname_valid_full (flags 0: < IFNAMSIZ, not numeric, not ./../all/default)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L769   ifname_valid_char (33..126 except ':' '/' '%')
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L797-L799   parse_ifindex() >= 0 → refused (no IFNAME_VALID_NUMERIC)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L497   safe_atoi (via parse_ifindex: value must be > 0)
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
 * True if parse_ifindex() would take [name] as an interface index: safe_atoi() reads it (sign, `0x`,
 * `0b`, `0o`, leading-`0` octal) and the result is a positive `int`. ifname_valid() with no flags
 * refuses those, so `0x5`, `+5`, `0b101`, `0o7`, `010` and `0b+1` are invalid, while `-5`, `+0`, `0x0`,
 * `+0b1` (no prefix stripping after a sign) and out-of-range `0x80000000` are ordinary names.
 * Checked against systemd's own parser (sd-check) at 0f0682b773.
 */
private fun bindToDeviceIsIfindex(name: String): Boolean =
    seccompBpfCgroupSafeAtoiValue(name)?.let { it in 1..Int.MAX_VALUE.toLong() } ?: false

/** [IFNAME_WITH_SPECIFIERS], additionally refusing the spellings [bindToDeviceIsIfindex] matches. */
private class BindToDeviceIfnameTerminal(private val inner: RegexTerminal) : TerminalCombinator {
    override fun SyntacticMatch(value: String, offset: Int): MatchResult = inner.SyntacticMatch(value, offset)

    override fun SemanticMatch(value: String, offset: Int): MatchResult {
        val result = inner.SemanticMatch(value, offset)
        if (result.tokens.size == 1 && bindToDeviceIsIfindex(result.tokens[0])) return NoMatch.copy(longestMatch = offset)
        return result
    }

    override fun parse(value: String, offset: Int): Sequence<ParseStep> = inner.parse(value, offset).map { step ->
        if (step is Parse) {
            step.copy(tokens = step.tokens.map { it.copy(terminal = this, valid = it.valid && !bindToDeviceIsIfindex(it.text)) })
        } else {
            step
        }
    }

    override fun toString(): String = "BindToDeviceIfname($inner)"
}

/**
 * Validator for `BindToDevice=`.
 *
 * `*` (like the empty value) clears the setting. Anything else is run through unit_full_printf() and
 * must then be a valid interface name per ifname_valid() with no flags — so alternative names longer
 * than 15 characters are refused, unlike BindNetworkInterface=. An invalid name is "ignoring", so it is
 * flagged. Specifiers are allowed (`BindToDevice=%i` in a template socket is a real pattern).
 *
 * Numeric spellings that parse_ifindex() takes as an index (`0x5`, `+5`, `0b101`) are refused like
 * plain all-digit names, see [bindToDeviceIsIfindex].
 *
 * Deliberately lenient: a name containing specifiers is not length-checked.
 *
 * Covers: Socket.BindToDevice.
 */
class ConfigParseSocketBindtodeviceOptionValue : SimpleGrammarOptionValues(
    "config_parse_socket_bindtodevice",
    AlternativeCombinator(
        SequenceCombinator(LiteralChoiceTerminal("*"), EOF()),
        SequenceCombinator(BindToDeviceIfnameTerminal(IFNAME_WITH_SPECIFIERS), EOF()),
    )
)
