package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.PRINTF_CHAR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.printfCharExcluding

/*
 * XAttrEntryPoint= / XAttrListen= / XAttrAccept= ([Socket]).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.socket.html#XAttrEntryPoint=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L2269-L2327   config_parse_xattr (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/string-util.c#L1049   split_pair (first "=")
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/unit-printf.c#L249   unit_full_printf (name and value)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/specifier.c#L33   specifier_printf (unknown alphanumeric specifier = -EBADSLT)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-74-AUX-UTILS.socket-xattr.sh#L55-L57   user.varlink=entrypoint / listen / server
 * linux  https://man7.org/linux/man-pages/man7/xattr.7.html   (the user. namespace)
 */

/**
 * Validator for the socket extended-attribute settings, `NAME=VALUE`.
 *
 * config_parse_xattr() splits at the first `=` (split_pair — no `=` at all is "Failed to parse extended
 * attribute expression, ignoring"), expands specifiers in both halves, and then requires the expanded
 * name to start with `user.` (the only xattr namespace an unprivileged socket inode can carry, see
 * xattr(7)). Everything after the first `=` is the value and may contain further `=` or be empty.
 *
 * The only other failure is specifier expansion: an unknown alphanumeric `%x` is an error, while `%%`,
 * a known specifier, or `%` before a non-alphanumeric character is fine ([PRINTF_CHAR]).
 *
 * Deliberately strict on one corner: the name must start with a literal `user.`. A name that only
 * becomes `user.…` after specifier expansion is theoretically possible but not realistic.
 *
 * Covers: Socket.XAttrEntryPoint, Socket.XAttrListen, Socket.XAttrAccept.
 *
 * The empty assignment resets the list and never reaches the grammar.
 */
class ConfigParseXattrOptionValue : SimpleGrammarOptionValues(
    "config_parse_xattr",
    SequenceCombinator(
        LiteralChoiceTerminal("user."),
        RegexTerminal("[^=]*", "${printfCharExcluding("=")}*"),
        LiteralChoiceTerminal("="),
        RegexTerminal("[\\s\\S]*", "$PRINTF_CHAR*"),
        EOF()
    )
)
