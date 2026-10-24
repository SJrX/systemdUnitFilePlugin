package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.whitespaceList

/*
 * [Service]/[Socket]/[Mount]/[Swap] TemporaryFileSystem=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.exec.html#TemporaryFileSystem=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L5116-L5185   config_parse_temporary_filesystems (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10            extract_first_word (EXTRACT_UNQUOTE, then ':' with EXTRACT_DONT_COALESCE_SEPARATORS)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/unit-printf.c#L253             unit_path_printf (specifiers)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L34          path_simplify_and_warn (PATH_CHECK_ABSOLUTE)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/namespace.c#L3347             temporary_filesystem_add (options stored verbatim)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-execute/exec-temporaryfilesystem-options.service#L10
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-execute/exec-temporaryfilesystem-ro.service#L35
 */

// Words are split with EXTRACT_UNQUOTE and WITHOUT EXTRACT_RETAIN_ESCAPE, so (unlike BindPaths=) a
// backslash escapes any following character, including a blank, both inside and outside quotes.
private const val TMPFS_CHUNK = """(?:[^\s:"'\\]|"(?:[^"\\]|\\[\s\S])*"|'(?:[^'\\]|\\[\s\S])*'|\\[\s\S])"""
private const val TMPFS_OPTIONS_CHUNK = """(?:[^\s"'\\]|"(?:[^"\\]|\\[\s\S])*"|'(?:[^'\\]|\\[\s\S])*'|\\[\s\S])"""

// The mount point: absolute after specifier expansion, with no ".." component.
private val TMPFS_MOUNT_POINT = RegexTerminal(
  "$TMPFS_CHUNK+",
  """(?!(?:[^\s:]*/)?\.\.(?:[/\s:"']|$))(?=["']*[/%])$TMPFS_CHUNK+(?![^\s:])""",
)

// Everything after the first ':' is the option string, stored verbatim (it may itself contain ':').
private val TMPFS_OPTIONS = RegexTerminal("$TMPFS_OPTIONS_CHUNK+", "$TMPFS_OPTIONS_CHUNK+")

/**
 * Validator for `TemporaryFileSystem=` (config_parse_temporary_filesystems, ltype 0).
 *
 * A whitespace-separated list of `PATH[:OPTIONS]`. PATH is cut at the first ':' and must be absolute
 * after specifier expansion, with no `..` component; OPTIONS is everything after that ':' (mount
 * options such as `ro,mode=0700`), stored verbatim and only interpreted at mount time, so it is not
 * checked. `PATH:` (empty options) is accepted. An invalid mount point is logged and skipped.
 *
 * Lenient corners: quoted runs are opaque (a ':' inside quotes is really the separator), and `%`
 * specifiers are not checked.
 */
class ConfigParseTemporaryFilesystemsOptionValue : SimpleGrammarOptionValues(
  "config_parse_temporary_filesystems",
  whitespaceList(
    SequenceCombinator(TMPFS_MOUNT_POINT, ZeroOrOne(SequenceCombinator(COLON, ZeroOrOne(TMPFS_OPTIONS))))
  )
)
