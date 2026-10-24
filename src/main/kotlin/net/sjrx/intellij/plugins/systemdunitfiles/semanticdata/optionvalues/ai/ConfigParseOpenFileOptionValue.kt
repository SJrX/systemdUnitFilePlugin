package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.OneOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [Service] OpenFile=
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.service.html#OpenFile=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L6743-L6777   config_parse_open_file (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/open-file.c#L12-L68   open_file_parse (a repeated flag is EINVAL, #L48-L49)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/open-file.c#L70   open_file_validate (absolute path, valid fd name, at most one of read-only/append/truncate)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/open-file.c#L134   open_file_flags_table
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/fd-util.c#L554   fdname_is_valid (printable ASCII except ':', at most 255)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10   extract_first_word (":" with EXTRACT_DONT_COALESCE_SEPARATORS|EXTRACT_CUNESCAPE; "," with 0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/escape.c#L104   cunescape_one
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-open-file.c#L7   TEST(open_file_parse)
 */

// A C escape as cunescape_one() understands it; any other backslash sequence makes open_file_parse fail.
private const val C_ESCAPE = """\\(?:[abfnrtv\\"'s]|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4}|U[0-9a-fA-F]{8}|[0-7]{3})"""

// The path: everything up to the first ':' (a ':' inside it has to be escaped, e.g. \x3a). Must be
// absolute; path_is_valid() allows ".." here, unlike most unit-file paths. No specifier expansion.
private val OPEN_FILE_PATH = RegexTerminal("""(?:[^:\\]|\\[\s\S]?)+""", """(?:/|$C_ESCAPE)(?:[^:\\]|$C_ESCAPE)*(?=:|$)""")

// The fd name: printable ASCII other than ':'. Empty means "use the file name".
private val OPEN_FILE_FDNAME = RegexTerminal("""(?:[^:\\]|\\[\s\S]?)+""", """(?:[\x20-\x7E&&[^:\\]]|$C_ESCAPE){1,255}(?=:|$)""")

// open_file_parse refuses a flag given twice, and open_file_validate refuses more than one of
// read-only/append/truncate, so a valid list holds at most one access flag and at most one `graceful`.
// Exact literals rather than FlexibleLiteralChoiceTerminal: the two choices must not match each other's
// words syntactically, or the original (greedy, non-backtracking) engine commits to the wrong branch.
private val OPEN_FILE_ACCESS_FLAG = LiteralChoiceTerminal("read-only", "append", "truncate")
private val OPEN_FILE_GRACEFUL_FLAG = LiteralChoiceTerminal("graceful")
private val COMMA = LiteralChoiceTerminal(",")

// extract_first_word(…, ",", 0) coalesces separators, so stray commas anywhere are harmless (even a
// field of nothing but commas, which yields no flags).
private val OPEN_FILE_OPTIONS = SequenceCombinator(
  ZeroOrMore(COMMA),
  ZeroOrOne(
    SequenceCombinator(
      AlternativeCombinator(
        SequenceCombinator(OPEN_FILE_ACCESS_FLAG, ZeroOrOne(SequenceCombinator(OneOrMore(COMMA), OPEN_FILE_GRACEFUL_FLAG))),
        SequenceCombinator(OPEN_FILE_GRACEFUL_FLAG, ZeroOrOne(SequenceCombinator(OneOrMore(COMMA), OPEN_FILE_ACCESS_FLAG))),
      ),
      ZeroOrMore(COMMA),
    )
  ),
)

/**
 * Validator for `[Service] OpenFile=` (config_parse_open_file, the only key using it).
 *
 * `PATH[:FD-NAME[:OPTIONS]]`, split on the first two colons with C escapes decoded (so a literal colon
 * in the path is written `\x3a`) and nothing allowed after a third field except one trailing `:`:
 *
 *  - PATH must be absolute;
 *  - FD-NAME, if given, is printable ASCII without `:` (at most 255 characters); an empty one defaults
 *    to the file name;
 *  - OPTIONS is a comma-separated list of at most one of `read-only`, `append`, `truncate` and at
 *    most one `graceful`, in either order (a repeated flag, or two of read-only/append/truncate, is
 *    refused by systemd).
 *
 * Not checked: a path such as `/` whose file name cannot serve as the default fd name. Unlike most
 * settings this one performs no specifier expansion, so `%` is an ordinary character.
 *
 * An empty assignment resets the list and is handled before this grammar is consulted.
 */
class ConfigParseOpenFileOptionValue : SimpleGrammarOptionValues(
  "config_parse_open_file",
  SequenceCombinator(
    OPEN_FILE_PATH,
    ZeroOrOne(
      SequenceCombinator(
        COLON,
        ZeroOrOne(OPEN_FILE_FDNAME),
        // The third extract (EXTRACT_DONT_COALESCE_SEPARATORS) consumes the colon that ends it, so one
        // trailing `:` after the options leaves nothing behind and passes the isempty(v) check.
        ZeroOrOne(SequenceCombinator(COLON, OPEN_FILE_OPTIONS, ZeroOrOne(COLON))),
      )
    ),
    EOF(),
  )
)
