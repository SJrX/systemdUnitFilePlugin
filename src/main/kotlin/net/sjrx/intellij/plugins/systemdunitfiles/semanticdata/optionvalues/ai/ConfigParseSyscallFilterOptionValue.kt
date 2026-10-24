package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Combinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ERRNO_OR_SECCOMP_ACTION
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SYSCALL_NAME
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
 *
 * sd-check at 0f0682b773 (one value per file), a `~` word after the first position:
 *   .nspawn  `@system-service ~@mount`, `@system-service ~@bogus`, `~~@mount`, `@system-service ~@mount:EPERM`: accepted
 *            (nspawn's parser stores every word unchecked).
 *   .service `@system-service ~@mount`, `read ~write`, `~~@mount`, `@system-service ~@bogus`: "System call ~@mount is not
 *            known, ignoring." (the same libseccomp-lookup warning as `madeupcall`); `@system-service ~@mount:EPERM`:
 *            "Allow-listed system calls cannot take error number"; `~@bogus` (marker + unknown group): "Unknown system
 *            call group".
 */

/** `@group` or a bare system call name. */
private val NAME: Combinator = SYSCALL_OR_GROUP

/**
 * A word starting with `~` after the first position of the value (`@system-service ~@mount`). Only
 * `rvalue[0]` is the deny-list marker in both parsers, so this word is taken literally as the name `~@mount`.
 *
 *  - nspawn's parser stores it unchecked; at container start seccomp_add_syscall_filter_item() cannot resolve
 *    it and drops it with a debug message. That is a runtime-only rejection, so it is accepted here; issue #565
 *    tracks a separate warning for it.
 *  - the unit-file parser hands it to libseccomp, which does not know it ("System call ~@mount is not known,
 *    ignoring."). That is the same unknown-name warning this grammar already leaves to the shape check for
 *    `madeupcall`.
 *
 * The validator registry is keyed only by (parser, ltype), and both files register (config_parse_syscall_filter,
 * 0), so the two cannot be told apart; the lenient nspawn behaviour wins. After the `~` the name is not looked
 * up as a group by either parser, so any group-shaped word is accepted (`~@bogus`).
 */
private val TILDE_NAME: Combinator = SequenceCombinator(
    LiteralChoiceTerminal("~"),
    AlternativeCombinator(
        SequenceCombinator(LiteralChoiceTerminal("@"), RegexTerminal("[^\\s,:]+", "[a-z0-9_-]+")),
        SYSCALL_NAME,
    ),
)

/** One allow-list entry after the first position: a name or a literal `~` word, with no `:errno`. */
private val ALLOW_ENTRY: Combinator = AlternativeCombinator(TILDE_NAME, NAME)

/** One deny-list entry: a name or a literal `~` word, optionally `:errno`/`:kill`. */
private val DENY_ENTRY: Combinator = SequenceCombinator(
    AlternativeCombinator(TILDE_NAME, NAME),
    ZeroOrOne(SequenceCombinator(LiteralChoiceTerminal(":"), ERRNO_OR_SECCOMP_ACTION)),
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
 *    The parser refuses an unknown group ("Unknown system call group, ignoring"), so it is flagged.
 *  - a system call name, resolved by libseccomp for the native architecture. Unknown names are only
 *    logged and the set is architecture-dependent, so names are only checked for shape (lower-case
 *    letters, digits, `_`); `DONOTEXIST` is flagged, `madeupcall` is not.
 *  - a word starting with `~` after the first position (`@system-service ~@mount`), which both parsers take
 *    as a literal, never-matching name. nspawn only drops it at container start, so it is accepted (see
 *    [TILDE_NAME]; issue #565 tracks a warning for it).
 *  - in deny-list mode only, any of those followed by `:` and an errno name (case-insensitive,
 *    `EPERM`), a number 0…4095 (decimal, `0x` hex or `0` octal) or `kill`. parse_syscall_and_errno
 *    rejects an empty name (`:EPERM`), an empty or junk errno (`uname:`, `uname:EINVALaaa`) and an
 *    out-of-range number (`uname:4096`). Without the `~`, config_parse_syscall_filter refuses any
 *    errno suffix ("Allow-listed system calls cannot take error number, ignoring").
 *
 * nspawn: the same `~` and separators, but each word is stored verbatim and only handed to libseccomp /
 * the group table at container start. nspawn does not understand `:errno` at all (`uname:EPERM` is just an
 * unresolvable name there). Because the two parsers share one registration, this grammar applies the core
 * rules above to nspawn files too, except for the non-first `~` word, where nspawn's lenient behaviour wins.
 *
 * Covers: Service.SystemCallFilter, Socket.SystemCallFilter, Mount.SystemCallFilter,
 * Swap.SystemCallFilter (load-fragment-gperf) and Exec.SystemCallFilter (nspawn-gperf).
 *
 * The empty assignment resets the filter and never reaches the grammar.
 */
class ConfigParseSyscallFilterOptionValue : SimpleGrammarOptionValues(
    "config_parse_syscall_filter",
    AlternativeCombinator(
        // Deny list: `rvalue[0] == '~'`; a further `~` word (`~~@mount`) is a literal name.
        SequenceCombinator(
            LiteralChoiceTerminal("~"),
            ZeroOrOne(WHITESPACE_OR_COMMA_SEPARATOR),
            ZeroOrOne(
                SequenceCombinator(
                    DENY_ENTRY,
                    ZeroOrMore(SequenceCombinator(WHITESPACE_OR_COMMA_SEPARATOR, DENY_ENTRY)),
                    ZeroOrOne(WHITESPACE_OR_COMMA_SEPARATOR),
                )
            ),
            EOF(),
        ),
        // Allow list starting with a word: the first word cannot start with `~` (it would be the marker).
        SequenceCombinator(
            NAME,
            ZeroOrMore(SequenceCombinator(WHITESPACE_OR_COMMA_SEPARATOR, ALLOW_ENTRY)),
            ZeroOrOne(WHITESPACE_OR_COMMA_SEPARATOR),
            EOF(),
        ),
        // Allow list starting with separators (`,~read`): every word may start with `~`.
        SequenceCombinator(
            WHITESPACE_OR_COMMA_SEPARATOR,
            ZeroOrOne(
                SequenceCombinator(
                    ALLOW_ENTRY,
                    ZeroOrMore(SequenceCombinator(WHITESPACE_OR_COMMA_SEPARATOR, ALLOW_ENTRY)),
                    ZeroOrOne(WHITESPACE_OR_COMMA_SEPARATOR),
                )
            ),
            EOF(),
        ),
    )
)
