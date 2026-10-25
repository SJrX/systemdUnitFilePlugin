package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import com.intellij.codeInspection.ProblemsHolder
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Combinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarOptionValue
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
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-mount.c#L813-L816   mount_bind returns its error, and mount_custom (#L1076-L1101) aborts the container start
 * oracle steve-local/systemd-tools/bin/sd-check (fuzz-nspawn-settings at 0f0682b773): `Bind=/a:/b:bogus`, `Bind=/a:/b:rbind:x`,
 *        `BindReadOnly=/a:/b:ro`, `Bind=/a:/b:rbind,bogus`, `Bind=/a:/b::`, `Bind=/a:/b:has space` are all accepted
 *        (no output; the container then fails to start, #565); `Bind=/a:relative:rbind` → "Invalid bind mount specification ... Invalid argument".
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-13-NSPAWN.nspawn.sh#L264-L287
 */

/** The options parse_mount_bind_options() knows. Listed for completion and highlighting. */
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
 * Everything after the second colon, for colouring and completion and for locating path errors.
 * bind_mount_parse() strdup()s it verbatim (no unescaping, no checks), so the settings parser accepts any
 * text. The known-option list is tried first so that a valid list is coloured and completed as options;
 * otherwise the whole rest is taken as one opaque option string. Unknown options are checked separately
 * with [BIND_OPTIONS_STRICT].
 */
private val BIND_OPTIONS_FIELD = PreferValidAlternativeCombinator(
    SequenceCombinator(BIND_OPTIONS, EOF()),
    SequenceCombinator(RegexTerminal("""[\s\S]+""", """[\s\S]+"""), EOF()),
)

/**
 * One comma-separated word of the option string, valid only if parse_mount_bind_options() knows it. Any other
 * word (`bogus`, `ro`, `rbind:x`, `has space`, `:`) makes it fail with "Invalid bind mount option" when the
 * container starts (nspawn-mount.c#L789-L791), and the container does not start.
 */
private val BIND_OPTION_WORD_STRICT = RegexTerminal(
    """[^,]+""",
    """(?:rbind|norbind|idmap|noidmap|rootidmap|owneridmap)(?=,|\z)""",
)

/** The option string as parse_mount_bind_options() reads it, with every word checked. */
private val BIND_OPTIONS_STRICT = SequenceCombinator(
    ZeroOrMore(COMMA),
    ZeroOrOne(SequenceCombinator(BIND_OPTION_WORD_STRICT, ZeroOrMore(SequenceCombinator(OneOrMore(COMMA), BIND_OPTION_WORD_STRICT)))),
    ZeroOrMore(COMMA),
    EOF(),
)

/** A lone path is both source and destination, so it must be absolute (optionally `+`-prefixed). */
private val SINGLE_PATH = RegexTerminal("""(?:[^:\\]|\\[\s\S])+""", """\+?\\?/(?:[^:\\]|\\[\s\S])*""")

/** `[SOURCE:]DEST[:OPTIONS]` or a lone `PATH`, with [optionsField] after the second colon. */
private fun bindGrammar(optionsField: Combinator) = AlternativeCombinator(
    SequenceCombinator(
        ZeroOrOne(COLON_FIELD_SOURCE_PATH),
        COLON,
        COLON_FIELD_ABSOLUTE_PATH,
        AlternativeCombinator(
            SequenceCombinator(COLON, optionsField),
            EOF(),
        ),
    ),
    SequenceCombinator(SINGLE_PATH, EOF()),
)

/**
 * The same value with every option checked. Only consulted once the paths are known to be valid, so any
 * semantic error it finds is an unknown option, and the message can say so.
 */
private val BIND_WITH_STRICT_OPTIONS = object : GrammarOptionValue("config_parse_bind", bindGrammar(BIND_OPTIONS_STRICT)) {
    override fun invalidValueMessage(key: String, badValue: String): String {
        val readOnlyHint = if (badValue == "ro" || badValue == "rw") " For a read-only mount use BindReadOnly= instead." else ""
        return "$key: '$badValue' is not a bind mount option, so systemd-nspawn refuses to start the container " +
            "(\"Invalid bind mount option\"). Options are a comma-separated list of rbind, norbind, idmap, noidmap, " +
            "rootidmap, owneridmap.$readOnlyHint"
    }
}

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
 * The settings parser stores OPTIONS unchecked, but when the container starts parse_mount_bind_options()
 * refuses anything other than a comma-separated list of `rbind`, `norbind`, `idmap`, `noidmap`, `rootidmap`,
 * `owneridmap` (e.g. `bogus`, `rbind:x`, `ro`) and the container fails to start, so those are flagged too
 * (#565). The paths are checked first, with the lenient [BIND_OPTIONS_FIELD]; only a value whose paths are
 * fine is then checked with [BIND_WITH_STRICT_OPTIONS], so each value gets at most one problem.
 */
class ConfigParseBindOptionValue : SimpleGrammarOptionValues(
    "config_parse_bind",
    bindGrammar(BIND_OPTIONS_FIELD),
) {
    override fun generateProblemDescriptors(property: UnitFilePropertyType, holder: ProblemsHolder) {
        val before = holder.resultCount
        super.generateProblemDescriptors(property, holder)
        if (holder.resultCount == before) {
            BIND_WITH_STRICT_OPTIONS.generateProblemDescriptors(property, holder)
        }
    }
}
