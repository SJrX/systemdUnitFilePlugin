package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/*
 * [IPv6RoutePrefix] Preference= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Preference=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-radv.c#L1161-L1190   config_parse_route_prefix_preference
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-radv.c#L1557-L1581   config_parse_router_preference
 */

/**
 * Validator for [IPv6RoutePrefix] Preference= (.network).
 *
 * C Function: config_parse_route_prefix_preference(0), which hands the value to
 * config_parse_router_preference: `medium`, `normal` and `default` all mean medium, plus `high` and
 * `low` (exact, case-sensitive matches). `normal` and `default` were missing before (sd-check accepts
 * `Preference=normal`).
 */
class ConfigParseRoutePrefixPreferenceOptionValue : SimpleGrammarOptionValues(
  "config_parse_route_prefix_preference",
  SequenceCombinator(
    FlexibleLiteralChoiceTerminal("high", "medium", "normal", "default", "low"),
    EOF(),
  ),
)
