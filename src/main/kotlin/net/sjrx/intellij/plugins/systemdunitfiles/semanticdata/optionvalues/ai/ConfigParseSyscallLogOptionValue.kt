package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SYSCALL_OR_GROUP
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WHITESPACE_OR_COMMA_SEPARATOR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * SystemCallLog= ([Service]/[Socket]/[Mount]/[Swap]).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.exec.html#SystemCallLog=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L3384-L3454   config_parse_syscall_log (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10   extract_first_word (WHITESPACE ",", no flags)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/seccomp-util.c#L1293   seccomp_parse_syscall_filter (errno_num = -1)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/seccomp-util.c#L334   syscall_filter_sets[] (group names)
 * linux  https://man7.org/linux/man-pages/man2/syscalls.2.html
 */

/** A possibly-empty whitespace/comma separated list, with stray separators allowed at either end. */
private val LIST = SequenceCombinator(
    ZeroOrOne(WHITESPACE_OR_COMMA_SEPARATOR),
    ZeroOrOne(
        SequenceCombinator(
            SYSCALL_OR_GROUP,
            ZeroOrMore(SequenceCombinator(WHITESPACE_OR_COMMA_SEPARATOR, SYSCALL_OR_GROUP)),
            ZeroOrOne(WHITESPACE_OR_COMMA_SEPARATOR),
        )
    ),
)

/**
 * Validator for `SystemCallLog=`.
 *
 * Same list syntax as `SystemCallFilter=` (see [ConfigParseSyscallFilterOptionValue]): an optional
 * leading `~` (log everything *except* the listed calls), then system call names and `@group`s split
 * on whitespace and commas, with stray separators tolerated. Unlike SystemCallFilter=, each word goes
 * straight to seccomp_parse_syscall_filter() with no parse_syscall_and_errno() step, so there is no
 * `:errno` suffix — `uname:EPERM` would be looked up as a system call called `uname:EPERM`, fail to
 * resolve, and be dropped with a warning; it is flagged here.
 *
 * Unknown `@group`s are flagged (warned and ignored by systemd). Bare names are only shape-checked
 * (lower-case letters, digits, `_`), because libseccomp resolves them per native architecture.
 *
 * Covers: Service.SystemCallLog, Socket.SystemCallLog, Mount.SystemCallLog, Swap.SystemCallLog.
 *
 * The empty assignment resets the list and never reaches the grammar.
 */
class ConfigParseSyscallLogOptionValue : SimpleGrammarOptionValues(
    "config_parse_syscall_log",
    // `~` is a separate alternative rather than ZeroOrOne("~"): the classic engine's ZeroOrOne probes its
    // inner combinator a second time and reports that as the furthest point reached, which on `~~x`
    // produced a highlight range past the end of the value.
    AlternativeCombinator(
        SequenceCombinator(LiteralChoiceTerminal("~"), LIST, EOF()),
        SequenceCombinator(LIST, EOF()),
    )
)
