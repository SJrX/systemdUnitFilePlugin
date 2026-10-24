package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Service]/[Socket]/[Mount]/[Swap] RootHash=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.exec.html#RootHash=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L1715-L1771   config_parse_exec_root_hash (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/path-util.h#L30                path_is_absolute (just `p[0] == '/'`)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/hexdecoct.c#L109-L158         unhexmem_full / unhex_next (whitespace skipped, pairs of hex digits)
 */

/**
 * Validator for `RootHash=` (config_parse_exec_root_hash, ltype 0).
 *
 * Either a path to a file holding the root hash, or the hash itself in hexadecimal:
 *  - the path branch is taken whenever path_is_absolute(), i.e. the value starts with `/`; the rest is
 *    stored verbatim (no specifier expansion, no normalization check), so nothing more is checked;
 *  - otherwise unhexmem() must decode it: an even number of hex digits (either case), whitespace
 *    anywhere ignored, and at least sizeof(sd_id128_t) = 16 bytes (32 digits) — shorter is "too short".
 *
 * Because there is no specifier expansion, `%t/foo.roothash` is not absolute and fails as hex — so it is
 * flagged, matching systemd.
 */
class ConfigParseExecRootHashOptionValue : SimpleGrammarOptionValues(
  "config_parse_exec_root_hash",
  SequenceCombinator(
    AlternativeCombinator(
      RegexTerminal("""/[\s\S]*""", """/[\s\S]*"""),
      RegexTerminal("""[^/][\s\S]*""", """(?:\s*[0-9a-fA-F]\s*[0-9a-fA-F]){16,}\s*(?![\s\S])"""),
    ),
    EOF(),
  )
)
