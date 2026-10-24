package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NON_EMPTY_BASE64_DATA
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Service]/[Socket]/[Mount]/[Swap] StandardInputData=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.exec.html#StandardInputData=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L1271-L1327   config_parse_exec_input_data (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/hexdecoct.c#L755              unbase64mem_full
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/hexdecoct.c#L528              unbase64char (standard and URL-safe alphabets)
 */

/**
 * Validator for `StandardInputData=` (config_parse_exec_input_data, ltype 0).
 *
 * Base64 as unbase64mem() decodes it: whitespace anywhere is ignored (so a value continued over several
 * lines with a trailing `\` works), standard (`+/`) or URL-safe (`-_`) alphabet, complete 4-character
 * groups, `=` padding only in the last group and only where the discarded bits are zero. No escapes or
 * specifiers are resolved. The 64 MiB cumulative cap (EXEC_STDIN_DATA_MAX) is not checked.
 */
class ConfigParseExecInputDataOptionValue : SimpleGrammarOptionValues(
  "config_parse_exec_input_data",
  SequenceCombinator(NON_EMPTY_BASE64_DATA, EOF())
)
