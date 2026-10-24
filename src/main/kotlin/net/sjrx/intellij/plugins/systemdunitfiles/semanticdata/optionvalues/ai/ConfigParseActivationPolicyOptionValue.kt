package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Link] ActivationPolicy= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#ActivationPolicy=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-network.c#L1201         config_parse_activation_policy (0), DEFINE_CONFIG_PARSE_ENUM
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-network.c#L1191-L1198   activation_policy_table
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-activation-policy.network.d/always-down.conf#L2
 */

/**
 * Validator for `[Link] ActivationPolicy=`: exactly one of `up`, `always-up`, `manual`, `always-down`,
 * `down`, `bound` (plain DEFINE_STRING_TABLE_LOOKUP — case-sensitive, no boolean).
 */
class ConfigParseActivationPolicyOptionValue : SimpleGrammarOptionValues(
    "config_parse_activation_policy",
    SequenceCombinator(
        FlexibleLiteralChoiceTerminal("up", "always-up", "manual", "always-down", "down", "bound"),
        EOF(),
    )
)
