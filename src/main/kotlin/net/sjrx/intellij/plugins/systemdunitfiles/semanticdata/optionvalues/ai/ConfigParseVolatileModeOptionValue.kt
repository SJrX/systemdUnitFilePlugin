package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.enumWithBoolean

/*
 * [Files] Volatile= (.nspawn).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#Volatile=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd-nspawn.html#--volatile=MODE
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-settings.c#L246   config_parse_volatile_mode (0), DEFINE_CONFIG_PARSE_ENUM
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/volatile-util.c#L36-L43   volatile_mode_table, DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/string-table.c#L32-L43   string_table_lookup_from_string_with_boolean
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L23-L46   parse_boolean
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-13-NSPAWN.nspawn.sh#L539
 */

/**
 * Validator for the .nspawn `[Files] Volatile=` setting (config_parse_volatile_mode, ltype 0).
 *
 * volatile_mode_from_string() is a DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN lookup: any parse_boolean()
 * spelling (case-insensitive — false means `no`, true means `yes`), or one of the table names `no`,
 * `yes`, `state`, `overlay`, matched case-sensitively.
 */
class ConfigParseVolatileModeOptionValue : SimpleGrammarOptionValues(
    "config_parse_volatile_mode",
    enumWithBoolean("no", "yes", "state", "overlay"),
)
