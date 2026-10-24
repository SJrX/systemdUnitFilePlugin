package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.whitespaceList

/*
 * [Service]/[Socket]/[Mount]/[Swap] CoredumpFilter=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.exec.html#CoredumpFilter=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L802-L838     config_parse_exec_coredump_filter (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/coredump-util.c#L38-L81     coredump_filter_mask_from_string (words split on WHITESPACE, flags 0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/coredump-util.c#L24-L34     coredump_filter_table
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.h#L95             safe_atoux64 (base 16)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-coredump-util.c#L26-L70  TEST(coredump_filter_mask_from_string)
 * linux  https://man7.org/linux/man-pages/man5/core.5.html
 */

// A raw mask: strtoull(…, 16) — an optional 0x prefix, at most 16 significant hex digits (uint64_t).
// The trailing lookahead keeps this from matching a hex-looking prefix of a longer word (the "de" of
// "default"), so it and the names never claim the same word.
private val COREDUMP_FILTER_MASK = RegexTerminal(
  """\+?[0-9a-fA-FxX]+(?![a-zA-Z0-9-])""",
  """\+?(?:0[xX])?0*[0-9a-fA-F]{1,16}(?![a-zA-Z0-9-])""",
)

/**
 * Validator for `CoredumpFilter=` (config_parse_exec_coredump_filter, ltype 0).
 *
 * A whitespace-separated list of mapping-type names (`private-anonymous`, `shared-anonymous`,
 * `private-file-backed`, `shared-file-backed`, `elf-headers`, `private-huge`, `shared-huge`,
 * `private-dax`, `shared-dax`), the specials `default` and `all`, or raw masks, which are always
 * hexadecimal (`11` is 0x11; `0x1101` is fine) and must fit 64 bits. Names are matched exactly
 * (streq / string table, case-sensitive). One bad word makes systemd ignore the whole assignment.
 */
class ConfigParseExecCoredumpFilterOptionValue : SimpleGrammarOptionValues(
  "config_parse_exec_coredump_filter",
  whitespaceList(
    AlternativeCombinator(
      COREDUMP_FILTER_MASK,
      FlexibleLiteralChoiceTerminal(
        "default", "all",
        "private-anonymous", "shared-anonymous", "private-file-backed", "shared-file-backed",
        "elf-headers", "private-huge", "shared-huge", "private-dax", "shared-dax",
      ),
    )
  )
)
