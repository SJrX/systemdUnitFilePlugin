package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ROUTE_TABLE

/*
 * [VRF] Table= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#Table=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/vrf.c#L10-L39   config_parse_vrf_table (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-util.c#L435-L464   manager_get_route_table_from_string
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-util.c#L427-L431   route_table_table (default, main, local)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-util.c#L496-L616   config_parse_route_table_names (what a user-defined name may be)
 */

/**
 * Validator for `[VRF] Table=` (.netdev).
 *
 * C function: config_parse_vrf_table → manager_get_route_table_from_string: `default`, `main`,
 * `local`, a name defined with `RouteTable=` in networkd.conf, or a non-zero uint32 (safe_atou32, so hex
 * works; `0` is refused with ERANGE). See [ROUTE_TABLE].
 *
 * Because table names are user-defined, any word without whitespace or `:` that is not all digits is
 * accepted as a potential name. What is flagged: `0` and out-of-range numbers, values containing
 * whitespace, and `name:number` pairs. An invalid value is logged and ignored by systemd.
 *
 * There is no isempty() branch, so an empty `Table=` is a parse error in systemd (logged and ignored), not
 * a reset. The plugin never runs a grammar on an empty value, so it is not flagged here.
 */
class ConfigParseVrfTableOptionValue : SimpleGrammarOptionValues(
    "config_parse_vrf_table",
    ROUTE_TABLE
)
