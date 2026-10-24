package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [Service]/[Socket]/[Mount]/[Swap] ImportCredential=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.exec.html#ImportCredential=GLOB
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L4868-L4926   config_parse_import_credential (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10            extract_first_word (':' with EXTRACT_DONT_COALESCE_SEPARATORS; no unquoting)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/creds-util.c#L55             credential_glob_valid (glob, after unit_cred_printf)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/creds-util.c#L49             credential_name_valid (rename; filename_is_valid && fdname_is_valid)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/fd-util.c#L554                fdname_is_valid (ASCII 0x20-0x7E, no ':', <= 255)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-creds.c#L301-L328         TEST(credential_glob_valid)
 */

// A credential glob: printable ASCII without '/' or ':' and without the glob characters `*?[]`, except
// one trailing '*'; a bare '*' matches everything. The prefix must itself be a valid name, so '.', '..',
// '.*' and '..*' are refused.
private val CREDENTIAL_GLOB = RegexTerminal(
  "[^:]+",
  """\*|(?!\.\.?\*?(?::|$))[\x20-\x7E&&[^/:*?\[\]]]{1,255}\*?(?![^:])""",
)

// The rename target: a credential name, checked verbatim (no specifier expansion), and — being the
// whole remainder of the value — it may not contain another ':'.
private val CREDENTIAL_RENAME = RegexTerminal("[^:]+", """(?!\.\.?$)[\x20-\x7E&&[^/:]]{1,255}(?![^:])""")

/**
 * Validator for `ImportCredential=` (config_parse_import_credential, ltype 0).
 *
 * `GLOB[:NAME]`. GLOB is a credential name optionally ending in a single `*` (or just `*`); NAME, if
 * present, is the credential name to rename matches to (`foo.*:bar.` is the documented prefix-rename
 * form). The value is not split on whitespace and not unquoted — credential names may contain spaces
 * — and an empty NAME (`foo:`) is accepted. Credential names are printable ASCII (0x20–0x7E) without
 * `/` or `:`, at most 255 characters, and not `.` or `..`.
 *
 * Lenient corner: `%` specifiers in GLOB are not checked (they are ordinary characters as far as the
 * name rules go).
 */
class ConfigParseImportCredentialOptionValue : SimpleGrammarOptionValues(
  "config_parse_import_credential",
  SequenceCombinator(
    CREDENTIAL_GLOB,
    ZeroOrOne(SequenceCombinator(COLON, ZeroOrOne(CREDENTIAL_RENAME))),
    EOF(),
  )
)
