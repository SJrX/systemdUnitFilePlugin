package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.enumWithBoolean

/*
 * [Exec] ResolvConf= (.nspawn).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#ResolvConf=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd-nspawn.html#--resolv-conf=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-settings.c#L830   config_parse_resolv_conf (0), DEFINE_CONFIG_PARSE_ENUM
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-settings.c#L832-L850   resolv_conf_mode_table, DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/string-table.c#L32-L43   string_table_lookup_from_string_with_boolean
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L23-L46   parse_boolean
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-13-NSPAWN.nspawn.sh#L532
 */

/**
 * Validator for the .nspawn `[Exec] ResolvConf=` setting (config_parse_resolv_conf, ltype 0).
 *
 * resolv_conf_mode_from_string() is a DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN lookup: any
 * parse_boolean() spelling (case-insensitive — false means `off`, true means `auto`), or one of the
 * table names, matched case-sensitively: `off`, `copy-host`, `copy-static`, `copy-uplink`, `copy-stub`,
 * `replace-host`, `replace-static`, `replace-uplink`, `replace-stub`, `bind-host`, `bind-static`,
 * `bind-uplink`, `bind-stub`, `delete`, `auto`.
 */
class ConfigParseResolvConfOptionValue : SimpleGrammarOptionValues(
    "config_parse_resolv_conf",
    enumWithBoolean(
        "off",
        "copy-host", "copy-static", "copy-uplink", "copy-stub",
        "replace-host", "replace-static", "replace-uplink", "replace-stub",
        "bind-host", "bind-static", "bind-uplink", "bind-stub",
        "delete", "auto",
    ),
)
