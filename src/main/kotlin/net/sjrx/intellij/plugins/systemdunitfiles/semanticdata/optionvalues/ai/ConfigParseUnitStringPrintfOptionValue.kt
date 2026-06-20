package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for the ~35 keys parsed by config_parse_unit_string_printf (Unit.Description,
 * Service.SyslogIdentifier, Service.PAMName, Socket.SmackLabel, Mount.Type, Mount.Options, ...).
 *
 * C function: config_parse_unit_string_printf in src/core/load-fragment.c. It runs
 * unit_full_printf() (specifier expansion) and then config_parse_string() (store as-is). There is
 * therefore NO constraint on the resulting value beyond it being an ordinary (UTF-8) string -- so
 * the only thing we can meaningfully validate is the specifier syntax: every '%' must be either a
 * literal "%%" or a known unit specifier. unit_full_printf() returns an error (and the assignment
 * is dropped) on an unknown specifier, so flagging those is faithful to systemd.
 *
 * Valid specifiers are the unit table plus COMMON_SYSTEM/CREDS/TMP_SPECIFIERS from
 * src/core/unit-printf.c and src/shared/specifier.h:
 *   i I j J n N p P f y Y c r R C d D E L S t h s   a A b B H l q m M o v w W   g G u U   T V
 *
 * Any other character (including non-ASCII, e.g. "Description=café") is accepted. The empty value
 * (which resets the field) is also accepted.
 */
class ConfigParseUnitStringPrintfOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_string_printf",
    SequenceCombinator(
        RegexTerminal(".*", "(?:[^%]|%%|%[iIjJnNpPfyYcrRCdDELSthsaAbBHlqmMovwWgGuUTV])*"),
        EOF()
    )
)