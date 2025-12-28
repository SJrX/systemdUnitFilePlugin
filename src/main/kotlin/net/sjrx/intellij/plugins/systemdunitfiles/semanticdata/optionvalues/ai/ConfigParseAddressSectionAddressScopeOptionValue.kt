package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for Address.Scope
 * C Function: config_parse_address_section(ADDRESS_SCOPE)
 * Used by Options: Address.Scope
 *
 * Valid values:
 * - "global" - valid everywhere on the network, even through a gateway
 * - "link" - only valid on this device, will not traverse a gateway
 * - "host" - only valid within the device itself
 * - Integer in the range 0-255
 *
 * Note: IPv4 only - IPv6 scope is automatically assigned by the kernel
 */
class ConfigParseAddressSectionAddressScopeOptionValue : SimpleGrammarOptionValues(
    "config_parse_address_section",
    SequenceCombinator(
        AlternativeCombinator(
            FlexibleLiteralChoiceTerminal("global", "link", "host"),
            IntegerTerminal(0, 256)
        ),
        EOF()
    )
)
