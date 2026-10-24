package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON_FIELD_ABSOLUTE_PATH
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON_FIELD_SOURCE_PATH
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [Files] Overlay= / OverlayReadOnly= (.nspawn).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#Overlay=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd-nspawn.html#--overlay=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-settings.c#L429-L453   config_parse_overlay (0 = Overlay=, 1 = OverlayReadOnly=)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-mount.c#L312-L385   overlay_mount_parse
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-mount.c#L86-L118   source_path_parse / source_path_parse_nullable
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/strv.c#L415-L448   strv_split_full (":", EXTRACT_DONT_COALESCE_SEPARATORS)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-13-NSPAWN.nspawn.sh#L545
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-13-NSPAWN.nspawn.sh#L303
 */

/**
 * A middle source path — one that is followed by another `:`. Same rules as [COLON_FIELD_SOURCE_PATH],
 * with a `(?=:)` lookahead so the repetition never even tentatively consumes the final DEST field: under
 * the classic matcher a tentative semantic match there would report a match length past the last
 * syntactic token and break error localization.
 */
private val MIDDLE_SOURCE_PATH = RegexTerminal(
    """(?:[^:\\]|\\[\s\S])+(?=:)""",
    """(?:\+\\?/(?:[^:\\]|\\[\s\S])*|(?!\+)(?:[^:\\]|\\[\s\S])+)(?=:)""",
)

/**
 * Validator for the .nspawn `[Files] Overlay=` and `OverlayReadOnly=` settings (config_parse_overlay;
 * ltype 1 only sets the read-only flag, so both share one grammar).
 *
 * overlay_mount_parse() splits the value on every unescaped `:` (strv_split_full with
 * EXTRACT_DONT_COALESCE_SEPARATORS, so empty fields are kept and `\:` embeds a colon). At least two
 * fields are required:
 *
 *  - two fields, `LOWER:UPPER` — both are source paths (non-empty; `+/abs` relative to the image, or any
 *    other path, a relative one being resolved against the working directory) and UPPER is also the
 *    mount point;
 *  - three or more, `LOWER…:UPPER:DEST` — every LOWER is a non-empty source path, UPPER is a source path
 *    or empty (a throw-away directory under /var/tmp, as in `+/var::/var`), and DEST must be absolute.
 *
 * The grammar only lets an empty field appear immediately before DEST, which is exactly the UPPER slot
 * when there are three or more fields. Parse errors are logged and the setting ignored.
 */
class ConfigParseOverlayOptionValue : SimpleGrammarOptionValues(
    "config_parse_overlay",
    AlternativeCombinator(
        SequenceCombinator(COLON_FIELD_SOURCE_PATH, COLON, COLON_FIELD_SOURCE_PATH, EOF()),
        SequenceCombinator(
            COLON_FIELD_SOURCE_PATH,
            COLON,
            ZeroOrMore(SequenceCombinator(MIDDLE_SOURCE_PATH, COLON)),
            ZeroOrOne(COLON),
            COLON_FIELD_ABSOLUTE_PATH,
            EOF(),
        ),
    )
)
