package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IntegerTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/**
 * Validator for `MTUBytes=` in `[Link]` (.network and .link) and `[NetDev]`, plus
 * `[Network] IPv6MTUBytes=` and `[DHCPv4] RouteMTUBytes=`.
 *
 * C function: config_parse_mtu (src/shared/conf-parser.c) → parse_mtu (src/basic/parse-util.c), which
 * runs parse_size() with base 1024 and then range-checks the result. The lower bound comes from the
 * ltype: AF_INET6 requires IPV6_MIN_MTU (1280) and AF_INET requires IPV4_MIN_MTU (68); AF_UNSPEC has
 * no minimum. The upper bound is UINT32_MAX throughout.
 *
 * parse_size() also accepts IEC suffixes and decimal fractions, which no [IntegerTerminal] can
 * range-check. The suffixed spelling is therefore accepted without a bound; the plain integer form —
 * what every real-world MTU uses — still gets one.
 */
open class ConfigParseMtuOptionValue(minimum: Long) : SimpleGrammarOptionValues(
    "config_parse_mtu",
    SequenceCombinator(
        AlternativeCombinator(
            SequenceCombinator(
                RegexTerminal("""[0-9]+(?:\.[0-9]+)?""", """[0-9]+(?:\.[0-9]+)?"""),
                LiteralChoiceTerminal("E", "P", "T", "G", "M", "K", "B"),
            ),
            IntegerTerminal(minimum, 4_294_967_296L),
        ),
        EOF()
    )
)

/** `MTUBytes=` with no family-specific minimum (ltype AF_UNSPEC). */
class ConfigParseMtuAnyOptionValue : ConfigParseMtuOptionValue(0)

/** `[DHCPv4] RouteMTUBytes=` (ltype AF_INET): at least IPV4_MIN_MTU. */
class ConfigParseMtuIpv4OptionValue : ConfigParseMtuOptionValue(68)

/** `[Network] IPv6MTUBytes=` (ltype AF_INET6): at least IPV6_MIN_MTU. */
class ConfigParseMtuIpv6OptionValue : ConfigParseMtuOptionValue(1280)
