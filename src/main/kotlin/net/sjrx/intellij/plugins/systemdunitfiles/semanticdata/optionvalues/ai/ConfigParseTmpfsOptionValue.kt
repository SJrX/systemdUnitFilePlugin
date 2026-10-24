package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON_FIELD_ABSOLUTE_PATH
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [Files] TemporaryFileSystem= (.nspawn).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#TemporaryFileSystem=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd-nspawn.html#--tmpfs=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-settings.c#L377-L401   config_parse_tmpfs (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-mount.c#L277-L310   tmpfs_mount_parse
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188   extract_first_word (":", EXTRACT_DONT_COALESCE_SEPARATORS)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-13-NSPAWN.nspawn.sh#L540-L541
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-13-NSPAWN.nspawn.sh#L297
 * linux  https://man7.org/linux/man-pages/man5/tmpfs.5.html
 */

/** The tmpfs mount options: the raw remainder, handed to mount(2) and only checked by the kernel. */
private val TMPFS_OPTIONS = RegexTerminal("""[\s\S]+""", """[\s\S]+""")

/**
 * Validator for the .nspawn `[Files] TemporaryFileSystem=` setting (config_parse_tmpfs, ltype 0).
 *
 * tmpfs_mount_parse() extracts the path up to the first unescaped `:` (backslash makes the next
 * character literal, so `\:` embeds a colon) and requires it to be absolute (path_is_absolute(): just
 * a leading `/`). Everything after the colon is kept verbatim as the mount option string; when it is
 * absent or empty `mode=0755` is used.
 *
 * Deliberately lenient: the option string is not checked — it is passed to the kernel's tmpfs, which
 * is the only thing that validates it.
 */
class ConfigParseTmpfsOptionValue : SimpleGrammarOptionValues(
    "config_parse_tmpfs",
    SequenceCombinator(
        COLON_FIELD_ABSOLUTE_PATH,
        ZeroOrOne(SequenceCombinator(COLON, ZeroOrOne(TMPFS_OPTIONS))),
        EOF(),
    )
)
