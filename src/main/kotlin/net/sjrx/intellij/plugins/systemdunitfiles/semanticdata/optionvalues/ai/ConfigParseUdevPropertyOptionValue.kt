package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [Link] Property= in a .link file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.link.html#Property=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/udev/net/link-config.c#L1810-L1878   config_parse_udev_property (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188   extract_first_word (EXTRACT_CUNESCAPE|EXTRACT_UNQUOTE)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/specifier.c#L33-L105   specifier_printf (link_specifier_table)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/env-util.c#L81-L102   env_assignment_is_valid (via udev_property_assignment_is_valid)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/libsystemd/sd-device/device-util.c#L219-L237   device_property_can_set
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/85-unmanaged.link#L18   Property=ID_NET_MANAGED_BY=io.systemd.Network
 */

// The properties udev reserves for itself (device_property_can_set), refused as assignment targets, plus
// the kernel's SYNTH_ARG_ prefix. The trailing lookahead makes each reserved word match only as the whole
// name, so `DEVPATHX` or `TAGS_EXTRA` are still fine.
private const val RESERVED_PROPERTY =
  """(?!(?:ACTION|SEQNUM|SYNTH_UUID|DEVPATH|DEVPATH_OLD|SUBSYSTEM|DEVTYPE|DRIVER|MODALIAS|DEVNAME|DEVMODE|""" +
    """DEVUID|DEVGID|MAJOR|MINOR|DISKSEQ|PARTN|IFINDEX|INTERFACE|INTERFACE_OLD|DEVLINKS|TAGS|CURRENT_TAGS|""" +
    """USEC_INITIALIZED|UDEV_DATABASE_VERSION)(?![A-Za-z0-9_%]))(?!SYNTH_ARG_)"""

// A link_specifier_table specifier (COMMON_SYSTEM_SPECIFIERS + COMMON_TMP_SPECIFIERS). `%%` is left out on
// purpose: it expands to a literal `%`, which no property name may contain.
private const val SPECIFIER = """%[aAbBHlqmMovwWTV]"""

// The property name: env_name_is_valid — bash-style, i.e. letters, digits and `_`, not starting with a
// digit. Specifiers are expanded before the check, so one may appear in the name too.
private val PROPERTY_NAME = RegexTerminal(
  """[A-Za-z0-9_%]+""",
  """$RESERVED_PROPERTY(?:[A-Za-z_]|$SPECIFIER)(?:[A-Za-z0-9_]|$SPECIFIER)*""",
)

private val EQUALS = LiteralChoiceTerminal("=")

// The value, in its raw (still-quoted, still-escaped) form. env_value_is_valid only requires valid UTF-8,
// so any character is fine; whitespace ends the word unless it is quoted or backslash-escaped.
private val RAW_VALUE = RegexTerminal(
  """(?:[^\s"'\\]|\\[\s\S]|"(?:[^"\\]|\\[\s\S])*"|'(?:[^'\\]|\\[\s\S])*')+""",
  """(?:[^\s"'\\]|\\[\s\S]|"(?:[^"\\]|\\[\s\S])*"|'(?:[^'\\]|\\[\s\S])*')+""",
)
private val DOUBLE_QUOTED_INNER = RegexTerminal("""(?:[^"\\]|\\[\s\S])+""", """(?:[^"\\]|\\[\s\S])+""")
private val SINGLE_QUOTED_INNER = RegexTerminal("""(?:[^'\\]|\\[\s\S])+""", """(?:[^'\\]|\\[\s\S])+""")

private fun quotedAssignment(quote: String, inner: RegexTerminal) = SequenceCombinator(
  LiteralChoiceTerminal(quote), PROPERTY_NAME, EQUALS, ZeroOrOne(inner), LiteralChoiceTerminal(quote), ZeroOrOne(RAW_VALUE),
)

// One word: NAME=VALUE, either plain (the value may still contain quoted runs) or with the whole
// assignment quoted from the start of the name.
private val PROPERTY_ASSIGNMENT = AlternativeCombinator(
  quotedAssignment("\"", DOUBLE_QUOTED_INNER),
  quotedAssignment("'", SINGLE_QUOTED_INNER),
  SequenceCombinator(PROPERTY_NAME, EQUALS, ZeroOrOne(RAW_VALUE)),
)

/**
 * Validator for `[Link] Property=` (.link).
 *
 * C function: config_parse_udev_property. A whitespace-separated list of `NAME=VALUE` udev property
 * assignments. Each word is split with extract_first_word(…, EXTRACT_CUNESCAPE|EXTRACT_UNQUOTE), so it may
 * be quoted (`"FOO=bar baz"` or `FOO="bar baz"`) or contain C escapes; specifiers from the link specifier
 * table (`%H`, `%v`, …) are expanded; then the assignment must pass env_assignment_is_valid — a
 * bash-style name (letters, digits, `_`, not starting with a digit), `=`, and any UTF-8 value, which may be
 * empty — and the name must not be one udev reserves (device_property_can_set: `ACTION`, `DEVPATH`,
 * `INTERFACE`, `TAGS`, …, or anything starting `SYNTH_ARG_`).
 *
 * Lenient corners: the escapes inside a value are not checked against cunescape_one's list (an unknown
 * escape such as `\q` makes systemd ignore the whole assignment), quotes that open in the middle of a
 * name are not modelled, and an unknown `%` specifier is not flagged. An invalid word is logged and
 * skipped by udev; an empty assignment clears the list.
 */
class ConfigParseUdevPropertyOptionValue : SimpleGrammarOptionValues(
  "config_parse_udev_property",
  SequenceCombinator(
    PROPERTY_ASSIGNMENT,
    ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), PROPERTY_ASSIGNMENT)),
    EOF()
  )
)
