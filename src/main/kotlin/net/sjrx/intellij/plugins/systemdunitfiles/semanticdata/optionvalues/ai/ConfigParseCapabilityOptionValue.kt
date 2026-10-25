package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import com.intellij.codeInspection.ProblemsHolder
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.GrammarOptionValue
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.CAPABILITY_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore

/*
 * [Exec] Capability= / AmbientCapability= / DropCapability= (.nspawn).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#Capability=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#AmbientCapability=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-settings.c#L276-L323   config_parse_capability (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188   extract_first_word (WHITESPACE, flags 0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/capability-list.c#L45-L66   capability_from_name
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L497-L520   safe_atoi (numeric capabilities)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/capability-util.h#L17   CAP_LIMIT
 * check  https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn.c#L1559-L1561   verify_arguments (refuses AmbientCapability=all after parsing)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-13-NSPAWN.nspawn.sh#L518-L520
 * linux  https://man7.org/linux/man-pages/man7/capabilities.7.html
 */

/** A capability number (0…CAP_LIMIT) read by capability_from_name()'s safe_atoi(). */
private val CAPABILITY_NUMBER = SafeAtoTerminal(
    signed = true,
    base = 0,
    flags = emptySet(),
    minInclusive = 0L,
    maxInclusive = 62L,
    acceptsWhitespace = false,
    acceptsMinus = true,
)

/** One capability word: the literal `all`, a capability number, or a capability name. */
private val CAPABILITY_WORD = AlternativeCombinator(
    LiteralChoiceTerminal("all"),
    CAPABILITY_NUMBER,
    CAPABILITY_NAME,
)

/** An AmbientCapability= word: no `all` (it falls to [CAPABILITY_NAME], which does not know it). */
private val AMBIENT_CAPABILITY_WORD = AlternativeCombinator(
    CAPABILITY_NUMBER,
    CAPABILITY_NAME,
)

/**
 * AmbientCapability=: the settings parser takes `all`, but verify_arguments() then refuses it and the
 * container does not start ("AmbientCapability= does not support the value all.", src/nspawn/nspawn.c#L1559-L1561).
 */
private val AMBIENT_CAPABILITY = object : GrammarOptionValue(
    "config_parse_capability",
    SequenceCombinator(
        AMBIENT_CAPABILITY_WORD,
        ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), AMBIENT_CAPABILITY_WORD)),
        EOF(),
    ),
) {
    override fun invalidValueMessage(key: String, badValue: String): String =
        if (badValue == "all") {
            "$key=all is refused by systemd-nspawn (\"AmbientCapability= does not support the value all\") and the container " +
                "does not start. List the capabilities explicitly, e.g. CAP_NET_ADMIN CAP_SYS_TIME; each must also be in Capability=."
        } else {
            super.invalidValueMessage(key, badValue)
        }
}

/**
 * Validator for the .nspawn `[Exec] Capability=`, `AmbientCapability=` and `DropCapability=` settings,
 * all of which use config_parse_capability with ltype 0.
 *
 * The value is a whitespace-separated list (extract_first_word with the default WHITESPACE separators,
 * so runs of whitespace coalesce). Each word is either:
 *
 *  - `all` — matched with a case-sensitive streq(), so `ALL` is not special and fails the name lookup;
 *  - a capability number — capability_from_name() first tries safe_atoi() (base 0, so `0x15` works)
 *    and accepts 0…CAP_LIMIT (62), even above the running kernel's last capability;
 *  - a capability name — looked up in a gperf table generated with `--ignore-case`, so `cap_net_admin`
 *    is as good as `CAP_NET_ADMIN`.
 *
 * An unknown word is logged and skipped rather than failing the whole line; it is still flagged here.
 *
 * AmbientCapability= also refuses `all`: the settings parser stores it, but verify_arguments() then fails
 * ("AmbientCapability= does not support the value all"), so the container does not start (#565). The three
 * keys share (parser, ltype), so [generateProblemDescriptors] picks the stricter grammar for that key.
 */
class ConfigParseCapabilityOptionValue : SimpleGrammarOptionValues(
    "config_parse_capability",
    SequenceCombinator(
        CAPABILITY_WORD,
        ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), CAPABILITY_WORD)),
        EOF(),
    )
) {
    override fun generateProblemDescriptors(property: UnitFilePropertyType, holder: ProblemsHolder) {
        when (property.key) {
            "AmbientCapability" -> AMBIENT_CAPABILITY.generateProblemDescriptors(property, holder)
            else -> super.generateProblemDescriptors(property, holder)
        }
    }
}
