package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.STRICT_USER_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore

/*
 * [Files] BindUser= (.nspawn).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#BindUser=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd-nspawn.html#--bind-user=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-settings.c#L979-L1024   config_parse_bind_user (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188   extract_first_word (WHITESPACE, flags 0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/user-util.c#L718-L832   valid_user_group_name (flags 0 = strict)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-13-NSPAWN.nspawn.sh#L631-L632
 */

/**
 * Validator for the .nspawn `[Files] BindUser=` setting (config_parse_bind_user, ltype 0).
 *
 * The value is a whitespace-separated list of user names (an empty value resets the list and never
 * reaches the grammar). Each name must pass valid_user_group_name() in its STRICT form (no
 * VALID_USER_RELAX): a letter or `_`, then letters, digits, `_` or `-`, at most 31 bytes (the utmp
 * ut_user field). Dots, leading digits or dashes, and numeric UIDs are all refused. On the first invalid
 * name the rest of the line is ignored with a warning.
 */
class ConfigParseBindUserOptionValue : SimpleGrammarOptionValues(
    "config_parse_bind_user",
    SequenceCombinator(
        STRICT_USER_NAME,
        ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), STRICT_USER_NAME)),
        EOF(),
    )
)
