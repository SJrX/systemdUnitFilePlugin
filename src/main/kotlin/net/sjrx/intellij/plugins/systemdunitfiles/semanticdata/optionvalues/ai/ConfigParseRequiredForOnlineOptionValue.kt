package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NETWORK_DHCP_MISC_BOOLEAN_ANY_CASE
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [Link] RequiredForOnline= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#RequiredForOnline=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-network.c#L1010-L1052                  config_parse_required_for_online (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/libsystemd/sd-network/network-util.c#L94-L137          parse_operational_state_range
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/libsystemd/sd-network/network-util.c#L44-L56           link_operstate_table
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L23-L46                             parse_boolean
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-state-file-tests.network#L6
 *        https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-activation-policy.network.d/required-yes.conf#L2
 */

// link_operstate_table, compared with streq (case-sensitive).
private val OPERSTATE = FlexibleLiteralChoiceTerminal(
    "missing", "off", "no-carrier", "dormant", "degraded-carrier", "carrier", "degraded", "enslaved", "routable",
)

/**
 * Validator for `[Link] RequiredForOnline=` (config_parse_required_for_online).
 *
 * Tried first as an operational-state range — `MIN`, `MIN:`, `MIN:MAX` or `:MAX` over the names in
 * link_operstate_table (split on the first `:`; a missing MIN means `missing`, a missing MAX means
 * `routable`) — and failing that as a boolean (case-insensitive).
 *
 * parse_operational_state_range also refuses a range whose MIN is above its MAX (`routable:off`); that
 * compares table positions rather than spelling and is not modelled (lenient).
 */
class ConfigParseRequiredForOnlineOptionValue : SimpleGrammarOptionValues(
    "config_parse_required_for_online",
    AlternativeCombinator(
        SequenceCombinator(OPERSTATE, ZeroOrOne(SequenceCombinator(COLON, ZeroOrOne(OPERSTATE))), EOF()),
        SequenceCombinator(COLON, OPERSTATE, EOF()),
        SequenceCombinator(NETWORK_DHCP_MISC_BOOLEAN_ANY_CASE, EOF()),
    )
)
