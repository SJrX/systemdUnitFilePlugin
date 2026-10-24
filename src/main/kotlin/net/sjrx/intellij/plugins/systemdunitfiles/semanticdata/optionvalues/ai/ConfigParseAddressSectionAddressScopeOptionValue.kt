package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/*
 * [Address] Scope= (.network)
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#Scope=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-address.c#L2230-L2258   config_parse_address_scope
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-route-util.c#L358-L366   route_scope_table, WITH_FALLBACK(…, UINT8_MAX)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/string-table.c#L65-L80   string_table_lookup_from_string_fallback (safe_atou)
 */

/**
 * Validator for [Address] Scope= (.network).
 *
 * C Function: config_parse_address_section(ADDRESS_SCOPE), i.e. config_parse_address_scope →
 * route_scope_from_string(). That is an exact (case-sensitive) lookup in route_scope_table — `global`,
 * `site`, `link`, `host`, `nowhere` — with a fallback to safe_atou() (base 0) for a number up to 255.
 * `site` and `nowhere` were missing before (sd-check accepts `Scope=site`).
 */
class ConfigParseAddressSectionAddressScopeOptionValue : SimpleGrammarOptionValues(
  "config_parse_address_section",
  SequenceCombinator(
    PreferValidAlternativeCombinator(
      FlexibleLiteralChoiceTerminal("global", "site", "link", "host", "nowhere"),
      SafeAtoTerminal(
        signed = false,
        base = 0,
        flags = emptySet(),
        minInclusive = 0L,
        maxInclusive = 255L,
        acceptsWhitespace = true,
        acceptsMinus = true,
      ),
    ),
    EOF(),
  ),
)
