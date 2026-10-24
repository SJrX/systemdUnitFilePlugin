package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RETAINED_ABSOLUTE_PATH_FIELD
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RETAINED_OPTIONAL_ABSOLUTE_PATH_FIELD
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.whitespaceList

/*
 * [Service]/[Socket]/[Mount]/[Swap] BindPaths= / BindReadOnlyPaths=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.exec.html#BindPaths=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L5187-L5305   config_parse_bind_paths (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10            extract_first_word (UNQUOTE|RETAIN_ESCAPE, then ':' with CUNESCAPE|UNESCAPE_SEPARATORS|DONT_COALESCE_SEPARATORS)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L240           extract_many_words_internal (4 slots: a 4th field is "Too many parameters")
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/escape.c#L104                 cunescape_one
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/unit-printf.c#L253             unit_path_printf (specifiers)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L34          path_simplify_and_warn (PATH_CHECK_ABSOLUTE)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-load-fragment.c#L459      TEST(config_parse_bind_paths)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-execute/exec-bindpaths.service#L27-L30
 */

/**
 * Validator for `BindPaths=` and `BindReadOnlyPaths=` (config_parse_bind_paths, ltype 0; the only
 * difference between the two keys is `strstr(lvalue, "ReadOnly")`).
 *
 * A whitespace-separated list of `[-]SOURCE[:DESTINATION[:OPTIONS]]` tuples:
 *  - SOURCE and DESTINATION are absolute paths after specifier expansion (so `%t/foo` is fine) with no
 *    `..` component; only SOURCE may carry the `-` "ignore if missing" prefix.
 *  - OPTIONS is `rbind`, `norbind`, or empty (`/a:/b:` is accepted — `isempty(options)` means rbind).
 *  - fields are split with EXTRACT_DONT_COALESCE_SEPARATORS, so an empty field is a field: `/a:` and
 *    `/a::rbind` have an empty, non-absolute destination and are rejected; a fourth field is too many.
 *  - a ':' inside a path is written `\:`; other backslashes must be valid C escapes.
 *
 * Every rejected tuple is logged by systemd and skipped ("…, ignoring"). Lenient corners: quoted runs
 * are opaque (see BatchExecCombinators.kt), and `%` specifiers are not checked.
 */
class ConfigParseBindPathsOptionValue : SimpleGrammarOptionValues(
  "config_parse_bind_paths",
  whitespaceList(
    SequenceCombinator(
      RETAINED_OPTIONAL_ABSOLUTE_PATH_FIELD,
      ZeroOrOne(
        SequenceCombinator(
          COLON,
          RETAINED_ABSOLUTE_PATH_FIELD,
          ZeroOrOne(SequenceCombinator(COLON, ZeroOrOne(FlexibleLiteralChoiceTerminal("rbind", "norbind")))),
        )
      ),
    )
  )
)
