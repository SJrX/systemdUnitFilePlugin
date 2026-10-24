package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BASE64_DATA
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Service]/[Socket]/[Mount]/[Swap] RootHashSignature=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.exec.html#RootHashSignature=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L1773-L1832   config_parse_exec_root_hash_sig (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/path-util.h#L30                path_is_absolute (just `p[0] == '/'`)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/hexdecoct.c#L755              unbase64mem_full (after the "base64:" prefix)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/hexdecoct.c#L528              unbase64char (standard and URL-safe alphabets)
 */

/**
 * Validator for `RootHashSignature=` (config_parse_exec_root_hash_sig, ltype 0).
 *
 * Either a path to a DER-encoded PKCS#7 signature (any value starting with `/`, stored verbatim with no
 * specifier expansion), or `base64:` followed by the signature in Base64 — whitespace ignored, standard
 * (`+/`) or URL-safe (`-_`) alphabet, complete 4-character groups, padding only at the end. The prefix
 * is matched case-sensitively (startswith). An empty payload (`base64:`) decodes to zero bytes and is
 * accepted by the parser.
 */
class ConfigParseExecRootHashSigOptionValue : SimpleGrammarOptionValues(
  "config_parse_exec_root_hash_sig",
  SequenceCombinator(
    AlternativeCombinator(
      RegexTerminal("""/[\s\S]*""", """/[\s\S]*"""),
      SequenceCombinator(LiteralChoiceTerminal("base64:"), BASE64_DATA),
    ),
    EOF(),
  )
)
