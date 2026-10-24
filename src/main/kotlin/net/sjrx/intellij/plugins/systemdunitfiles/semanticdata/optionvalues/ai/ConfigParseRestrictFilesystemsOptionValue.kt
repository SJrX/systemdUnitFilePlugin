package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FILESYSTEM_GROUP_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FILESYSTEM_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
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
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/bpf-restrict-fs.c#L117   fs_type_from_string at unit start ("Invalid filesystem name, ignoring"), runtime only
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/filesystem-sets.py#L9   NAME_TO_MAGIC / FILESYSTEM_SETS (the name tables)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-07-PID1.exec-context.sh#L351-L358   ("foo", "@foo" are logged and ignored)
 * linux  https://man7.org/linux/man-pages/man2/statfs.2.html   (the f_type magics the names map to)
 *
 * sd-check at 0f0682b773 (one value per file): accepted `EXT4`, `ext5`, `invalid`, `temporary`,
 * `~proc,sysfs,foo`, `@auxiliary-api btrfs  nfs @temporary unknown`, `ext"4"`, `ext4""`, `""`, `"a\"b"`,
 * `ext4 ~proc`, `ext4 ~@foo`, `~ ~@bogus`, `"~@bogus"`, `\~proc`, `foo.bar!`; refused `@unknowngroup`, `@`,
 * `@Basic-api`, `~@bogus`, `"@foo"`, `""@foo`, `\@foo`, `"@basic-api"x` ("Unknown filesystem group") and
 * `ext"4`, `ext4 "unterminated` ("Trailing garbage").
 */

// Regex pieces for one word as extract_first_word(…, WHITESPACE ",", EXTRACT_UNQUOTE) reads it: unquoted
// characters, `\x` escapes (the backslash is dropped and any character may follow, also inside quotes) and
// '…' / "…" runs whose quotes are removed. An unterminated quote is "Trailing garbage", so quoted runs must close.

/** Any one piece of a word. */
private const val WORD_PIECE = """[^\s,"'\\]|\\[\s\S]|"(?:[^"\\]|\\[\s\S])*"|'(?:[^'\\]|\\[\s\S])*'"""

/**
 * The first piece of a word, which must not unquote to `@` (bpf_restrict_fs_parse_filesystem() would look the
 * word up as a group and refuse an unknown one) and must not be a raw `~` (at the start of the value that is
 * the deny-list marker; elsewhere [TILDE_WORD] covers it).
 */
private const val FIRST_PIECE_NO_AT_NO_TILDE =
    """[^\s,"'\\@~]|\\[^@]|"(?:[^"\\@]|\\[^@])(?:[^"\\]|\\[\s\S])*"|'(?:[^'\\@]|\\[^@])(?:[^'\\]|\\[\s\S])*'"""

/** [FIRST_PIECE_NO_AT_NO_TILDE] after leading empty quotes, where a `~` is just a name character (`""~x` is `~x`). */
private const val FIRST_PIECE_NO_AT =
    """[^\s,"'\\@]|\\[^@]|"(?:[^"\\@]|\\[^@])(?:[^"\\]|\\[\s\S])*"|'(?:[^'\\@]|\\[^@])(?:[^'\\]|\\[\s\S])*'"""

/**
 * The known file system names as exact literals (completion, enum coloring). Not the lenient
 * [FILESYSTEM_NAME] itself: since any name is valid here, its "well-formed but unknown" token would only add
 * a second, invalid parse for every unknown word, and a list of unknown words would then explode into
 * 2^n parses on the new engine.
 */
private val KNOWN_FILESYSTEM_NAME = LiteralChoiceTerminal(*FILESYSTEM_NAME.choices)

/**
 * A known name, bare or quoted as a whole, filling a whole word. [ANY_FILESYSTEM_NAME] steps aside for these,
 * so they are matched by [KNOWN_FILESYSTEM_NAME] and each word has a single parse.
 */
private val KNOWN_NAME_WORD = """(?<q>["']?)(?:""" + KNOWN_FILESYSTEM_NAME.choices.joinToString("|") { Regex.escape(it) } +
    """)\k<q>(?=[\s,]|\z)"""

/**
 * Any word the unit-file parser stores as a plain file system name, i.e. one that does not unquote to a
 * leading `@`. The parser keeps such names unchecked: an unknown one (`EXT4`, `ext5`, `foo`) is only logged
 * and skipped when the unit starts and the BPF map is filled ("bpf-restrict-fs: Invalid filesystem name '%s',
 * ignoring.", src/core/bpf-restrict-fs.c#L117-L121), so it is accepted here. Issue #565 tracks a separate
 * warning for such names. A word that is wholly empty quotes (`""`) is stored as the empty name and accepted.
 */
private val ANY_FILESYSTEM_NAME = RegexTerminal(
    "(?!$KNOWN_NAME_WORD)(?:(?:\"\"|'')+(?:(?:$FIRST_PIECE_NO_AT)(?:$WORD_PIECE)*)?|(?:$FIRST_PIECE_NO_AT_NO_TILDE)(?:$WORD_PIECE)*)",
    "(?!$KNOWN_NAME_WORD)(?:(?:\"\"|'')+(?:(?:$FIRST_PIECE_NO_AT)(?:$WORD_PIECE)*)?|(?:$FIRST_PIECE_NO_AT_NO_TILDE)(?:$WORD_PIECE)*)",
)

/**
 * A word starting with a raw `~` anywhere except the very start of the value. Only `rvalue[0]` is the
 * deny-list marker, so `ext4 ~proc` or `~ ~@bogus` store the literal name `~proc` / `~@bogus`, which never
 * matches a file system at unit start. Accepted for the same reason as [ANY_FILESYSTEM_NAME] (#565).
 */
private val TILDE_WORD = RegexTerminal("~(?:$WORD_PIECE)*", "~(?:$WORD_PIECE)*")

/** A word that does not start with a raw `~`: an unknown name, a known name or `@group`, either possibly quoted. */
private val ENTRY_NOT_TILDE = AlternativeCombinator(
    // First, so the classic engine does not take a known-name prefix (`ext4` of `ext4foo`) and strand the rest.
    ANY_FILESYSTEM_NAME,
    SequenceCombinator(
        LiteralChoiceTerminal("\""),
        AlternativeCombinator(SequenceCombinator(LiteralChoiceTerminal("@"), FILESYSTEM_GROUP_NAME), KNOWN_FILESYSTEM_NAME),
        LiteralChoiceTerminal("\""),
    ),
    SequenceCombinator(
        LiteralChoiceTerminal("'"),
        AlternativeCombinator(SequenceCombinator(LiteralChoiceTerminal("@"), FILESYSTEM_GROUP_NAME), KNOWN_FILESYSTEM_NAME),
        LiteralChoiceTerminal("'"),
    ),
    SequenceCombinator(LiteralChoiceTerminal("@"), FILESYSTEM_GROUP_NAME),
    KNOWN_FILESYSTEM_NAME,
)

/** Any word, including one starting with a literal `~`. */
private val ENTRY = AlternativeCombinator(TILDE_WORD, ENTRY_NOT_TILDE)

/**
 * Validator for `RestrictFileSystems=`.
 *
 * An optional leading `~` (deny-list instead of allow-list), then words split on whitespace and commas
 * (separators coalesce; stray ones at either end are fine, and `~` alone is an empty deny list). Words are
 * unquoted with EXTRACT_UNQUOTE, so quotes and backslash escapes may appear anywhere in a word.
 *
 *  - A word that unquotes to `@group` must be one of filesystem-sets.py's FILESYSTEM_SETS (`@basic-api`,
 *    `@common-block`, `@network`, `@known`, …). The parser refuses an unknown group ("Unknown filesystem
 *    group, ignoring"), so it is flagged. The lookup is exact and case-sensitive.
 *  - Any other word is a file system name. The parser stores it unchecked. An unknown name (`EXT4`, `ext5`,
 *    `foo`, a literal `~proc` after the first word) is only rejected when the unit starts ("Invalid
 *    filesystem name, ignoring"), so it is accepted here. Issue #565 tracks a separate inspection that
 *    warns about values like this. Known names stay a separate alternative for completion and coloring.
 *
 * Known over-rejection: a word that only becomes `@group` through escaping or partial quoting
 * (`\@basic-api`, `'@'basic-api`, `""@basic-api`) is accepted by systemd but flagged here; `"@basic-api"`
 * quoted as a whole is accepted.
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
        // Deny list: `rvalue[0] == '~'`; every following word, even one starting with `~`, is a name.
        SequenceCombinator(
            LiteralChoiceTerminal("~"),
            ZeroOrOne(WHITESPACE_OR_COMMA_SEPARATOR),
            ZeroOrOne(
                SequenceCombinator(
                    ENTRY,
                    ZeroOrMore(SequenceCombinator(WHITESPACE_OR_COMMA_SEPARATOR, ENTRY)),
                    ZeroOrOne(WHITESPACE_OR_COMMA_SEPARATOR),
                )
            ),
            EOF(),
        ),
        // Allow list starting with a word: that first word cannot start with a raw `~` (it would be the marker).
        SequenceCombinator(
            ENTRY_NOT_TILDE,
            ZeroOrMore(SequenceCombinator(WHITESPACE_OR_COMMA_SEPARATOR, ENTRY)),
            ZeroOrOne(WHITESPACE_OR_COMMA_SEPARATOR),
            EOF(),
        ),
        // Allow list starting with separators (`,~proc`): every word may start with `~`.
        SequenceCombinator(
            WHITESPACE_OR_COMMA_SEPARATOR,
            ZeroOrOne(
                SequenceCombinator(
                    ENTRY,
                    ZeroOrMore(SequenceCombinator(WHITESPACE_OR_COMMA_SEPARATOR, ENTRY)),
                    ZeroOrOne(WHITESPACE_OR_COMMA_SEPARATOR),
                )
            ),
            EOF(),
        ),
    )
)
