package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Checked
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.HYPHEN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
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

// parse_uid: safe_atou32_full(s, 10 | SAFE_ATO_REFUSE_PLUS_MINUS | SAFE_ATO_REFUSE_LEADING_ZERO |
// SAFE_ATO_REFUSE_LEADING_WHITESPACE), so strict decimal, no `+`/`-`, no leading zero (except `0` itself) and
// no leading " \t\n\r" (a leading `\v` still gets through, as in C). 4294967295 is refused by uid_is_valid().
// The lower bound is the text before the first `-` (extract_first_word with "-"), so it cannot contain one.
private val UID_LOWER = SafeAtoTerminal(
    signed = false,
    base = 10,
    flags = setOf(
        SafeAtoTerminal.Flag.REFUSE_PLUS_MINUS,
        SafeAtoTerminal.Flag.REFUSE_LEADING_ZERO,
        SafeAtoTerminal.Flag.REFUSE_LEADING_WHITESPACE,
    ),
    minInclusive = 0L,
    maxInclusive = 4_294_967_294L,
    acceptsWhitespace = true,
    acceptsMinus = false,
)

// The upper bound is the rest of the value after that `-`.
private val UID_UPPER = SafeAtoTerminal(
    signed = false,
    base = 10,
    flags = setOf(
        SafeAtoTerminal.Flag.REFUSE_PLUS_MINUS,
        SafeAtoTerminal.Flag.REFUSE_LEADING_ZERO,
        SafeAtoTerminal.Flag.REFUSE_LEADING_WHITESPACE,
    ),
    minInclusive = 0L,
    maxInclusive = 4_294_967_294L,
    acceptsWhitespace = true,
    acceptsMinus = true,
)

// uid_is_valid() also refuses the 16-bit -1, 65535.
private val UID_RANGE = Checked(
    SequenceCombinator(
        Checked(UID_LOWER) { UID_LOWER.valueOf(it)?.toLong() != 65_535L },
        ZeroOrOne(SequenceCombinator(HYPHEN, Checked(UID_UPPER) { UID_UPPER.valueOf(it)?.toLong() != 65_535L })),
    )
) { text ->
  val dash = text.indexOf('-')
  val lo = if (dash >= 0) UID_LOWER.valueOf(text.substring(0, dash)) else null
  val hi = if (dash >= 0) UID_UPPER.valueOf(text.substring(dash + 1)) else null
  dash < 0 || lo == null || hi == null || lo <= hi
}

// A user name for getpwnam(): NSS is the judge, so anything without whitespace or `:` is accepted, as
// long as it does not look like a UID or UID range (those are judged by the numeric branch). The lenient
// shape stops only at systemd's WHITESPACE (" \t\n\r"), so under the classic engine a range such as
// `5-\v6` (a `\v` that parse_uid lets through) is not cut short at the `\v` and falls through to UID_RANGE.
private val USER_NAME = RegexTerminal("""[^ \t\n\r]+""", """(?![0-9][0-9-]*(?:\s|$))[^\s:]+""")

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
