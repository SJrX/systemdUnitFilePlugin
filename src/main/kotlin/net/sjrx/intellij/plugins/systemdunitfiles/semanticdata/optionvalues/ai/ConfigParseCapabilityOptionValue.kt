package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.CAPABILITY_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.safeAtoNumber

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

/** One capability word: the literal `all`, a capability number, or a capability name. */
private val CAPABILITY_WORD = AlternativeCombinator(
    LiteralChoiceTerminal("all"),
    safeAtoNumber(0, 63),
    CAPABILITY_NAME,
)

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
 * Deliberately lenient: the man page and nspawn itself refuse `all` for AmbientCapability= — that check
 * happens later, in verify_arguments() ("AmbientCapability= does not support the value all"), and the
 * grammar is shared by all three keys via (parser, ltype), so it cannot single that key out.
 */
class ConfigParseCapabilityOptionValue : SimpleGrammarOptionValues(
    "config_parse_capability",
    SequenceCombinator(
        CAPABILITY_WORD,
        ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), CAPABILITY_WORD)),
        EOF(),
    )
)
