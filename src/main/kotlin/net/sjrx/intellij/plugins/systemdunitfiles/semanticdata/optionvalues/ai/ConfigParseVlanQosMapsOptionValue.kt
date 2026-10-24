package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ExtractedWordTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.HYPHEN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WordEnd
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.netdevLinkParseRangeAccepts
import java.math.BigInteger

/*
 * [VLAN] EgressQOSMaps= / IngressQOSMaps= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#EgressQOSMaps=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/vlan.c#L120-L180   config_parse_vlan_qos_maps (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188   extract_first_word (EXTRACT_CUNESCAPE|EXTRACT_UNQUOTE)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L305-L340   parse_range
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478   safe_atou_full (base 0)
 */

// parse_range() bounds as safe_atou() reads them (any unsigned int). The word was split on whitespace, so no
// whitespace reaches safe_atou(). The lower bound is cut at the first `-`, so it never sees a minus; the upper
// bound is the rest of the word, which may (`1--0`).
private val QOS_LOWER = SafeAtoTerminal(
    signed = false,
    base = 0,
    flags = emptySet(),
    minInclusive = BigInteger.ZERO,
    maxInclusive = SafeAtoTerminal.UINT32_MAX,
    acceptsWhitespace = false,
    acceptsMinus = false,
)
private val QOS_UPPER = SafeAtoTerminal(
    signed = false,
    base = 0,
    flags = emptySet(),
    minInclusive = BigInteger.ZERO,
    maxInclusive = SafeAtoTerminal.UINT32_MAX,
    acceptsWhitespace = false,
    acceptsMinus = true,
)
private val QOS_RANGE = SequenceCombinator(QOS_LOWER, ZeroOrOne(SequenceCombinator(HYPHEN, QOS_UPPER)))

// The same bounds for a word whose quotes and escapes have been removed: quoted or escaped whitespace does reach
// safe_atou().
private val QOS_UNQUOTED_LOWER = SafeAtoTerminal(
    signed = false,
    base = 0,
    flags = emptySet(),
    minInclusive = BigInteger.ZERO,
    maxInclusive = SafeAtoTerminal.UINT32_MAX,
    acceptsWhitespace = true,
    acceptsMinus = false,
)
private val QOS_UNQUOTED_UPPER = SafeAtoTerminal(
    signed = false,
    base = 0,
    flags = emptySet(),
    minInclusive = BigInteger.ZERO,
    maxInclusive = SafeAtoTerminal.UINT32_MAX,
    acceptsWhitespace = true,
    acceptsMinus = true,
)

// One word: the structured `from[-to]` grammar, bare or quoted as a whole, for highlighting; then an
// [ExtractedWordTerminal] fallback for quotes and C escapes anywhere in the word.
private val QOS_MAPPING = AlternativeCombinator(
    SequenceCombinator(
        AlternativeCombinator(
            SequenceCombinator(LiteralChoiceTerminal("\""), QOS_RANGE, LiteralChoiceTerminal("\"")),
            SequenceCombinator(LiteralChoiceTerminal("'"), QOS_RANGE, LiteralChoiceTerminal("'")),
            QOS_RANGE,
        ),
        WordEnd("""\s"""),
    ),
    ExtractedWordTerminal("""\s""", cunescape = true, "range 0..4294967295") { word ->
        netdevLinkParseRangeAccepts(word, QOS_UNQUOTED_LOWER, QOS_UNQUOTED_UPPER)
    },
)

/**
 * Validator for `[VLAN] EgressQOSMaps=` and `IngressQOSMaps=` (.netdev).
 *
 * C function: config_parse_vlan_qos_maps. A whitespace-separated list of words (each may be quoted, and
 * may contain C escapes — the quotes and escapes are removed before parsing, see [ExtractedWordTerminal]);
 * every word goes to parse_range(), so it is `from-to` or a single number meaning `from == to`, each
 * side an unsigned int as safe_atou() reads it ([SafeAtoTerminal]: decimal, `0x` hex, octal, `0b`/`0o`).
 *
 * The man page says each integer is in 1…4294967294 and that `from` must be >= `to`; the parser
 * enforces neither (it stores whatever parse_range returns), so this follows the C and accepts the full
 * unsigned range and any order. A malformed word is logged and skipped by systemd.
 */
class ConfigParseVlanQosMapsOptionValue : SimpleGrammarOptionValues(
    "config_parse_vlan_qos_maps",
    SequenceCombinator(QOS_MAPPING, ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), QOS_MAPPING)), EOF())
)
