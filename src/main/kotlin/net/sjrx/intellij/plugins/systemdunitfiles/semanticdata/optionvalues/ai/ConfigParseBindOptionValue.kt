package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON_FIELD_ABSOLUTE_PATH
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON_FIELD_SOURCE_PATH
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.OneOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.PreferValidAlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [Files] Bind= / BindReadOnly= (.nspawn).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#Bind=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd-nspawn.html#--bind=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-settings.c#L351-L375   config_parse_bind (0 = Bind=, 1 = BindReadOnly=)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-mount.c#L234-L275   bind_mount_parse
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-mount.c#L86-L118   source_path_parse / source_path_parse_nullable
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L240-L281   extract_many_words (":", EXTRACT_DONT_COALESCE_SEPARATORS)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188   extract_first_word
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-mount.c#L759-L800   parse_mount_bind_options (only at container start, NOT at parse time)
 * oracle steve-local/systemd-tools/bin/sd-check (fuzz-nspawn-settings at 0f0682b773): `Bind=/a:/b:bogus`, `Bind=/a:/b:rbind:x`,
 *        `BindReadOnly=/a:/b:ro`, `Bind=/a:/b:rbind,bogus`, `Bind=/a:/b::`, `Bind=/a:/b:has space` are all accepted
 *        (no output); `Bind=/a:relative:rbind` → "Invalid bind mount specification ... Invalid argument".
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-13-NSPAWN.nspawn.sh#L264-L287
 */

/**
 * The options parse_mount_bind_options() knows. Listed first only for completion and highlighting: anything
 * else is accepted too (see BIND_OPTIONS_FIELD), because only the container start rejects it.
 */
private val BIND_OPTION = FlexibleLiteralChoiceTerminal("rbind", "norbind", "idmap", "noidmap", "rootidmap", "owneridmap")

private val COMMA = LiteralChoiceTerminal(",")

/**
 * The option string: split with extract_first_word(",", 0), which coalesces repeated commas and ignores
 * leading and trailing ones (so a string of nothing but commas means "no options").
 */
private val BIND_OPTIONS = SequenceCombinator(
    ZeroOrMore(COMMA),
    ZeroOrOne(SequenceCombinator(BIND_OPTION, ZeroOrMore(SequenceCombinator(OneOrMore(COMMA), BIND_OPTION)))),
    ZeroOrMore(COMMA),
)

/**
 * Everything after the second colon. bind_mount_parse() strdup()s it verbatim (no unescaping, no checks), so
 * any text is accepted at parse time. The known-option list is tried first so that a valid list is coloured
 * and completed as options; otherwise the whole rest is taken as one opaque option string.
 * parse_mount_bind_options() rejects unknown options only when the container starts ("Invalid bind mount
 * option"); GitHub issue #565 tracks a separate warning for those.
 */
private val BIND_OPTIONS_FIELD = PreferValidAlternativeCombinator(
    SequenceCombinator(BIND_OPTIONS, EOF()),
    SequenceCombinator(RegexTerminal("""[\s\S]+""", """[\s\S]+"""), EOF()),
)

/** A lone path is both source and destination, so it must be absolute (optionally `+`-prefixed). */
private val SINGLE_PATH = RegexTerminal("""(?:[^:\\]|\\[\s\S])+""", """\+?\\?/(?:[^:\\]|\\[\s\S])*""")

/**
 * Validator for the .nspawn `[Files] Bind=` and `BindReadOnly=` settings (config_parse_bind; ltype 1
 * only sets the read-only flag, so both share one grammar).
 *
 * bind_mount_parse() splits the value into at most two colon-separated fields with extract_many_words(":",
 * EXTRACT_DONT_COALESCE_SEPARATORS) — backslash makes the next character literal, so `\:` embeds a colon;
 * quotes and whitespace are ordinary characters — and keeps whatever follows the second colon verbatim as
 * the option string:
 *
 *  - `PATH` alone is used as both source and destination: it must be absolute, optionally prefixed with
 *    `+` (meaning "relative to the container image"), which is dropped for the destination.
 *  - `SOURCE:DEST[:OPTIONS]` — SOURCE may be empty (a temporary directory under /var/tmp), `+/abs`, or any
 *    other path (a relative one is resolved against the working directory). DEST must be absolute.
 *    `SOURCE:` (an empty DEST) is invalid; an empty OPTIONS after the second colon is ignored.
 *
 * OPTIONS is not inspected at config-parse time, so any option string is accepted here. Only when the
 * container starts does parse_mount_bind_options() refuse anything other than a comma-separated list of
 * `rbind`, `norbind`, `idmap`, `noidmap`, `rootidmap`, `owneridmap` (e.g. `bogus`, `rbind:x`, `ro`); this
 * validator follows the parser and accepts them, and GitHub issue #565 tracks a separate warning for
 * such runtime-only rejections. Parse errors are logged and the setting ignored.
 */
class ConfigParseBindOptionValue : SimpleGrammarOptionValues(
    "config_parse_bind",
    AlternativeCombinator(
        SequenceCombinator(
            ZeroOrOne(COLON_FIELD_SOURCE_PATH),
            COLON,
            COLON_FIELD_ABSOLUTE_PATH,
            AlternativeCombinator(
                SequenceCombinator(COLON, BIND_OPTIONS_FIELD),
                EOF(),
            ),
        ),
        SequenceCombinator(SINGLE_PATH, EOF()),
    )
)
