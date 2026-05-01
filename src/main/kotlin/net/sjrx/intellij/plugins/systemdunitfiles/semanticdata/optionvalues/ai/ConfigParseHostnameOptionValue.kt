package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for config_parse_hostname(0).
 * Used by Options: Exec.Hostname (.nspawn), DHCPv4.Hostname, DHCPv6.Hostname, DHCP.Hostname (.network).
 *
 * Mirrors hostname_is_valid(s, 0):
 *   - LDH chars only (letters, digits, hyphen)
 *   - dots between labels, no leading or consecutive dots
 *   - no leading or trailing hyphen on a label
 *   - no trailing dot (VALID_HOSTNAME_TRAILING_DOT not set)
 */
class ConfigParseHostnameOptionValue : SimpleGrammarOptionValues(
    "config_parse_hostname",
    SequenceCombinator(
        RegexTerminal(
            "[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?)*",
            "[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?)*"
        ),
        EOF()
    )
)
