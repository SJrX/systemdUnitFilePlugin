package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Combinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ERRNO_OR_SECCOMP_ACTION
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SYSCALL_OR_GROUP
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WHITESPACE_OR_COMMA_SEPARATOR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * SystemCallFilter= ([Service]/[Socket]/[Mount]/[Swap] and nspawn's [Exec]).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.exec.html#SystemCallFilter=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#SystemCallFilter=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L3290-L3382   config_parse_syscall_filter (0)
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-settings.c#L747-L790   config_parse_syscall_filter (0), nspawn's own function of the same name
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10   extract_first_word (WHITESPACE ",", no flags)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/seccomp-util.c#L2508   parse_syscall_and_errno
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/seccomp-util.c#L2688   seccomp_parse_errno_or_action
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L342   parse_errno
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/seccomp-util.c#L1293   seccomp_parse_syscall_filter
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/seccomp-util.c#L334   syscall_filter_sets[] (group names)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/seccomp-util.c#L1035   seccomp_add_syscall_filter_item (nspawn's consumer)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-seccomp.c#L56   TEST(parse_syscall_and_errno)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-parse-util.c#L729   TEST(parse_errno)
 * linux  https://man7.org/linux/man-pages/man2/syscalls.2.html
 * linux  https://man7.org/linux/man-pages/man3/errno.3.html
 */

/** One allow-list entry: a system call or `@group`, with no `:errno`. */
private val ALLOW_ENTRY: Combinator = SYSCALL_OR_GROUP

/** One deny-list entry: a system call or `@group`, optionally `:errno`/`:kill`. */
private val DENY_ENTRY: Combinator = SequenceCombinator(
    SYSCALL_OR_GROUP,
    ZeroOrOne(SequenceCombinator(LiteralChoiceTerminal(":"), ERRNO_OR_SECCOMP_ACTION)),
)

/** A possibly-empty separator-delimited list of [entry], with stray separators allowed at either end. */
private fun list(entry: Combinator): Combinator = SequenceCombinator(
    ZeroOrOne(WHITESPACE_OR_COMMA_SEPARATOR),
    ZeroOrOne(
        SequenceCombinator(
            entry,
            ZeroOrMore(SequenceCombinator(WHITESPACE_OR_COMMA_SEPARATOR, entry)),
            ZeroOrOne(WHITESPACE_OR_COMMA_SEPARATOR),
        )
    ),
)

/**
 * Validator for `SystemCallFilter=` — both systemd's (exec context of service/socket/mount/swap units)
 * and systemd-nspawn's (`[Exec]` in .nspawn files), which share the (config_parse_syscall_filter, 0)
 * pair but are different C functions.
 *
 * Syntax (core): an optional leading `~` (deny-list instead of allow-list), then a list of entries
 * split on whitespace and commas. extract_first_word skips leading separators and coalesces runs, so
 * `~ @clock @debug`, `read,write` and a trailing separator are all fine; `~` on its own is an
 * (empty) deny list. Each entry is
 *
 *  - `@group` — one of the predefined sets in syscall_filter_sets[] (`@system-service`, `@mount`, …).
 *    An unknown group is logged and ignored, so it is flagged.
 *  - a system call name, resolved by libseccomp for the native architecture. Unknown names are only
 *    logged and the set is architecture-dependent, so names are only checked for shape (lower-case
 *    letters, digits, `_`); `DONOTEXIST` is flagged, `madeupcall` is not.
 *  - in deny-list mode only, either of those followed by `:` and an errno name (case-insensitive,
 *    `EPERM`), a number 0…4095 (decimal, `0x` hex or `0` octal) or `kill`. parse_syscall_and_errno
 *    rejects an empty name (`:EPERM`), an empty or junk errno (`uname:`, `uname:EINVALaaa`) and an
 *    out-of-range number (`uname:4096`). Without the `~`, config_parse_syscall_filter refuses any
 *    errno suffix ("Allow-listed system calls cannot take error number, ignoring").
 *
 * nspawn: the same `~` and separators, but each word is handed verbatim to libseccomp / the group
 * table at container start (an unknown group fails the filter setup). nspawn does not understand
 * `:errno` at all (`uname:EPERM` is just an unresolvable name there); this grammar still accepts the
 * suffix after `~` for nspawn files, since the two parsers share one registration — lenient.
 *
 * Covers: Service.SystemCallFilter, Socket.SystemCallFilter, Mount.SystemCallFilter,
 * Swap.SystemCallFilter (load-fragment-gperf) and Exec.SystemCallFilter (nspawn-gperf).
 *
 * The empty assignment resets the filter and never reaches the grammar.
 */
class ConfigParseSyscallFilterOptionValue : SimpleGrammarOptionValues(
    "config_parse_syscall_filter",
    AlternativeCombinator(
        SequenceCombinator(LiteralChoiceTerminal("~"), list(DENY_ENTRY), EOF()),
        SequenceCombinator(list(ALLOW_ENTRY), EOF()),
    )
)
