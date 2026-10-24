package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.CIDR_SEPARATOR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore

/*
 * [DHCPServer] Timezone= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Timezone=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L2222-L2255   config_parse_timezone (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1596-L1655    verify_timezone
 * linux  https://man7.org/linux/man-pages/man5/tzfile.5.html
 */

// One path component of a zoneinfo name: verify_timezone allows ASCII letters, digits, `-`, `_`, `+`.
private val TZ_COMPONENT = RegexTerminal("""[^/]+""", """[A-Za-z0-9_+-]+""")

/**
 * Validator for `[DHCPServer] Timezone=` (.network).
 *
 * config_parse_timezone calls verify_timezone(), which accepts `UTC` unconditionally and otherwise
 * requires a relative zoneinfo path: components of ASCII letters, digits, `-`, `_` and `+`, separated by
 * single `/`, with no leading or trailing `/` (so no `.` or `..` components either). It then opens
 * `/usr/share/zoneinfo/<name>` and checks the TZif magic — that runtime check cannot be done here, so
 * a well-formed but non-existent zone such as `Mars/Olympus_Mons` is accepted.
 */
class ConfigParseTimezoneOptionValue : SimpleGrammarOptionValues(
    "config_parse_timezone",
    SequenceCombinator(
        TZ_COMPONENT,
        ZeroOrMore(SequenceCombinator(CIDR_SEPARATOR, TZ_COMPONENT)),
        EOF()
    )
)
