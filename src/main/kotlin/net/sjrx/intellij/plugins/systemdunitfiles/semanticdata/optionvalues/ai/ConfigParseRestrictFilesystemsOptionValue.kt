package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FILESYSTEM_GROUP_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FILESYSTEM_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WHITESPACE_OR_COMMA_SEPARATOR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * RestrictFileSystems= ([Service]/[Socket]/[Mount]/[Swap]).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.exec.html#RestrictFileSystems=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L3645-L3712   config_parse_restrict_filesystems (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10   extract_first_word (WHITESPACE ",", EXTRACT_UNQUOTE)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/bpf-restrict-fs.c#L212   bpf_restrict_fs_parse_filesystem
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/filesystems.c#L21   filesystem_set_find (group names)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/bpf-restrict-fs.c#L117   fs_type_from_string at BPF-map time ("Invalid filesystem name, ignoring")
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/filesystem-sets.py#L9   NAME_TO_MAGIC / FILESYSTEM_SETS (the name tables)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-07-PID1.exec-context.sh#L342   ("foo", "@foo" are logged and ignored)
 * linux  https://man7.org/linux/man-pages/man2/statfs.2.html   (the f_type magics the names map to)
 */

/** One file system name or `@group`. */
private val FILESYSTEM_OR_GROUP = AlternativeCombinator(
    SequenceCombinator(LiteralChoiceTerminal("@"), FILESYSTEM_GROUP_NAME),
    FILESYSTEM_NAME,
)

/** EXTRACT_UNQUOTE: a whole word may be wrapped in single or double quotes. */
private val ENTRY = AlternativeCombinator(
    SequenceCombinator(LiteralChoiceTerminal("\""), FILESYSTEM_OR_GROUP, LiteralChoiceTerminal("\"")),
    SequenceCombinator(LiteralChoiceTerminal("'"), FILESYSTEM_OR_GROUP, LiteralChoiceTerminal("'")),
    FILESYSTEM_OR_GROUP,
)

/** A possibly-empty whitespace/comma separated list, with stray separators allowed at either end. */
private val LIST = SequenceCombinator(
    ZeroOrOne(WHITESPACE_OR_COMMA_SEPARATOR),
    ZeroOrOne(
        SequenceCombinator(
            ENTRY,
            ZeroOrMore(SequenceCombinator(WHITESPACE_OR_COMMA_SEPARATOR, ENTRY)),
            ZeroOrOne(WHITESPACE_OR_COMMA_SEPARATOR),
        )
    ),
)

/**
 * Validator for `RestrictFileSystems=`.
 *
 * An optional leading `~` (deny-list instead of allow-list), then file system names and `@group`s
 * split on whitespace and commas (separators coalesce; stray ones at either end are fine, and `~` alone
 * is an empty deny list). Each word may be quoted, since the list is split with EXTRACT_UNQUOTE.
 *
 *  - `@group` must be one of filesystem-sets.py's FILESYSTEM_SETS (`@basic-api`, `@common-block`,
 *    `@network`, `@known`, …); an unknown group is warned about and ignored.
 *  - a bare name is stored unchecked by the unit-file parser, but when the unit starts each name is
 *    resolved with fs_type_from_string() against NAME_TO_MAGIC and an unknown one is logged and skipped
 *    — so it is flagged here. The lookup is a case-sensitive gperf table (`EXT4` is not `ext4`).
 *
 * Deliberately lenient: quote characters in the middle of a word (`ext"4"`, which EXTRACT_UNQUOTE would
 * also accept) are not modelled and get flagged.
 *
 * Covers: Service.RestrictFileSystems, Socket.RestrictFileSystems, Mount.RestrictFileSystems,
 * Swap.RestrictFileSystems.
 *
 * The empty assignment resets the list and never reaches the grammar.
 */
class ConfigParseRestrictFilesystemsOptionValue : SimpleGrammarOptionValues(
    "config_parse_restrict_filesystems",
    // `~` is a separate alternative rather than ZeroOrOne("~"): the classic engine's ZeroOrOne probes its
    // inner combinator a second time and reports that as the furthest point reached, which on `~~x`
    // produced a highlight range past the end of the value.
    AlternativeCombinator(
        SequenceCombinator(LiteralChoiceTerminal("~"), LIST, EOF()),
        SequenceCombinator(LIST, EOF()),
    )
)
