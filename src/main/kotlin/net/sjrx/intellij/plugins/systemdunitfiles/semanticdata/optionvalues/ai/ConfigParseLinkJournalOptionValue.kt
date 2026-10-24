package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.enumWithBoolean

/*
 * [Exec] LinkJournal= (.nspawn).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#LinkJournal=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd-nspawn.html#--link-journal=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-settings.c#L888-L910   config_parse_link_journal (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-settings.c#L852-L886   parse_link_journal
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L23-L46   parse_boolean
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-13-NSPAWN.nspawn.sh#L534
 */

/**
 * Validator for the .nspawn `[Exec] LinkJournal=` setting (config_parse_link_journal, ltype 0).
 *
 * parse_link_journal() compares with streq() against `auto`, `guest`, `host`, `try-guest` and
 * `try-host` (case-sensitive), and otherwise falls back to parse_boolean() (case-insensitive; true is
 * treated as `auto`, false as `no`). Invalid input is logged and ignored.
 */
class ConfigParseLinkJournalOptionValue : SimpleGrammarOptionValues(
    "config_parse_link_journal",
    enumWithBoolean("auto", "guest", "host", "try-guest", "try-host"),
)
