package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for DHCPServerStaticLease.Address
 * C Function: config_parse_dhcp_static_lease_address(0)
 * Used by Options: DHCPServerStaticLease.Address
 * 
 * Validates that the value is a valid IPv4 address.
 * The C implementation uses in_addr_from_string with AF_INET to parse IPv4 addresses only.
 * Note: The C code also checks that the address is not 0.0.0.0, but that semantic validation
 * occurs after parsing and is not enforced by this grammar validator.
 */
class ConfigParseDhcpStaticLeaseAddressOptionValue : SimpleGrammarOptionValues(
    "config_parse_dhcp_static_lease_address",
    SequenceCombinator(
        IPV4_ADDR,
        EOF()
    )
)
