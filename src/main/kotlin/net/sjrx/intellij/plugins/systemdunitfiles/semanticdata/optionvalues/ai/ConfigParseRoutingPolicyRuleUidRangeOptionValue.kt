package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Checked
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.HYPHEN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [RoutingPolicyRule] User= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#User=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-routing-policy-rule.c#L1582-L1633   config_parse_routing_policy_rule (ROUTING_POLICY_RULE_UID_RANGE)
 *        https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-routing-policy-rule.c#L1478-L1508   config_parse_routing_policy_rule_uid_range
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/user-util.c#L285-L369    get_user_creds
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/user-util.c#L97-L135     parse_uid_range
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/user-util.c#L66-L95      parse_uid (base 10, no sign, no leading zero, no whitespace)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/user-util.c#L51-L64      uid_is_valid (not 65535, not 4294967295)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-fibrule-uidrange.network
 */

private const val UID_LIMIT = 4_294_967_296L

private fun uidIsValid(text: String): Boolean {
  val v = text.toLongOrNull() ?: return false
  return v < UID_LIMIT && v != 65_535L && v != 4_294_967_295L
}

// parse_uid: strict decimal, no `+`/`-`, no leading zero (except `0` itself), and uid_is_valid().
private val UID = Checked(RegexTerminal("""[0-9]+""", """0|[1-9][0-9]*""")) { uidIsValid(it) }

private val UID_RANGE = Checked(SequenceCombinator(UID, ZeroOrOne(SequenceCombinator(HYPHEN, UID)))) { text ->
  val bounds = text.split('-')
  // toLongOrNull: an over-long digit run is already rejected by UID; do not crash on it here.
  val lo = bounds[0].toLongOrNull()
  val hi = bounds.getOrNull(1)?.toLongOrNull()
  bounds.size == 1 || lo == null || hi == null || lo <= hi
}

// A user name for getpwnam(): NSS is the judge, so anything without whitespace or `:` is accepted, as
// long as it does not look like a UID or UID range (those are judged by the numeric branch).
private val USER_NAME = RegexTerminal("""\S+""", """(?![0-9][0-9-]*(?:\s|$))[^\s:]+""")

/**
 * Validator for `[RoutingPolicyRule] User=` (.network) — the ROUTING_POLICY_RULE_UID_RANGE slot.
 *
 * config_parse_routing_policy_rule_uid_range first tries get_user_creds() (a user name, or a numeric UID
 * that exists in the user database), and falls back to parse_uid_range(): `UID` or `UID-UID`, each
 * read by parse_uid() — decimal only, no sign, no leading zeros, no whitespace, and not one of the
 * invalid UIDs 65535 / 4294967295 — with the lower bound not above the upper.
 *
 * User names can only be checked at runtime, so any token that is not purely digits and dashes is
 * accepted as a possible name. A digits-and-dashes token is judged strictly as a UID or UID range.
 */
class ConfigParseRoutingPolicyRuleUidRangeOptionValue : SimpleGrammarOptionValues(
    "config_parse_routing_policy_rule",
    SequenceCombinator(
        // Name first: its lenient shape also covers numbers, but it only validates non-numeric names, so
        // the classic matcher falls through to the numeric branch for those.
        AlternativeCombinator(USER_NAME, UID_RANGE),
        EOF()
    )
)
