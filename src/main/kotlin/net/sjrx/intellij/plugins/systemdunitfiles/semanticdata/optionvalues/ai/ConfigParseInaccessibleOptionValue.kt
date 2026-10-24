package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Files] Inaccessible= (.nspawn).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#Inaccessible=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd-nspawn.html#--inaccessible=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-settings.c#L403-L427   config_parse_inaccessible (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-mount.c#L387-L408   inaccessible_mount_parse
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/path-util.h#L30-L35   path_is_absolute
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-13-NSPAWN.nspawn.sh#L542-L543
 */

/**
 * Validator for the .nspawn `[Files] Inaccessible=` setting (config_parse_inaccessible, ltype 0).
 *
 * inaccessible_mount_parse() takes the whole value verbatim — no splitting, unquoting or unescaping, so
 * spaces, colons and backslashes are ordinary characters — and only requires path_is_absolute(), i.e. a
 * leading `/`. A relative path is logged and ignored.
 */
class ConfigParseInaccessibleOptionValue : SimpleGrammarOptionValues(
    "config_parse_inaccessible",
    SequenceCombinator(
        RegexTerminal("""[\s\S]+""", """/[\s\S]*"""),
        EOF(),
    )
)
