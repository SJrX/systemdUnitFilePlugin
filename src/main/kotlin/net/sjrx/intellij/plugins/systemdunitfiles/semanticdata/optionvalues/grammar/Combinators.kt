package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/*
 * Shared value grammars, each modelled on the systemd routine that actually parses that kind of value.
 *
 * Everything here is pinned to systemd a8e93919c3, which is the commit recorded in
 * systemd-build/build/last_commit_hash and therefore the one the plugin's own gperf and man data are
 * generated from. Browse it at https://github.com/systemd/systemd/blob/a8e93919c3.
 *
 * Two things worth knowing before adding to this file:
 *
 *  - Under the classic SyntacticMatch/SemanticMatch engine, AlternativeCombinator is first-full-match
 *    with NO backtracking: once a branch matches at an offset the enclosing sequence continues from
 *    there and never reconsiders. So order alternatives longest/most-specific first, and put a lenient
 *    shape matcher (a FlexibleLiteralChoiceTerminal, say) after the precise ones it could shadow.
 *
 *  - Reach for the C source rather than the man page when they disagree. The man page documents intent
 *    and is sometimes narrower than the parser: DuplicateAddressDetection= is documented as four family
 *    names but config_parse_address_dad accepts booleans too, and safe_atou* silently accept hex and
 *    octal because they pass base 0 to strtoul. The gperf files under systemd-build/build/ are the
 *    authority for which (parser, ltype) pairs exist and which keys use them.
 */

val BOOLEAN = FlexibleLiteralChoiceTerminal("1", "yes", "y", "true", "t", "on", "0", "no", "n", "false", "f", "off")
val BYTES = RegexTerminal("[0-9]+[a-zA-Z]*\\s*", "[0-9]+[KMGT]?\\s*")
val DEVICE = RegexTerminal("\\S+\\s*", "/[^\\u0000. ]+\\s*")
val IOPS = RegexTerminal("[0-9]+[a-zA-Z]*\\s*", "[0-9]+[KMGT]?\\s*")

// Time-suffix list mirrors systemd's extract_multiplier (parse_sec, parse_nsec).
// Longer alternatives MUST come first so that `min` is tried before `m`, etc.
private const val TIME_SUFFIX = "(?:seconds|minutes|months|second|minute|month|years|weeks|hours|usec|msec|nsec|year|week|hour|days|min|sec|day|hr|µs|μs|ns|us|ms|s|m|h|d|w|y|M)"
private const val TIME_NUMBER = "[0-9]+(?:\\.[0-9]+)?"
private const val TIME_ELEMENT = "$TIME_NUMBER\\s*$TIME_SUFFIX?"
private const val TIME_COMPOUND = "$TIME_ELEMENT(?:\\s+$TIME_ELEMENT)*"
val TIME_VALUE = AlternativeCombinator(
  FlexibleLiteralChoiceTerminal("infinity"),
  RegexTerminal(TIME_COMPOUND, TIME_COMPOUND)
)

var IPV4_OCTET = IntegerTerminal(0, 256)
val DOT = LiteralChoiceTerminal(".")
// Labeled so an address colours as one literal span rather than per-octet/dot (transparent to matching).
var IPV4_ADDR = Labeled(Role.LITERAL, SequenceCombinator(IPV4_OCTET, DOT, IPV4_OCTET, DOT, IPV4_OCTET, DOT, IPV4_OCTET))

val CIDR_SEPARATOR = LiteralChoiceTerminal("/")

val IPV4_ADDR_AND_PREFIX_LENGTH = SequenceCombinator(IPV4_ADDR, CIDR_SEPARATOR, IntegerTerminal(8, 33))
val IPV4_ADDR_AND_OPTIONAL_PREFIX_LENGTH = SequenceCombinator(IPV4_ADDR, ZeroOrOne(SequenceCombinator( CIDR_SEPARATOR, IntegerTerminal(8, 33))))

var IPV6_HEXTET = RegexTerminal("[0-9a-fA-F]{1,4}", "[0-9a-fA-F]{1,4}")
val COLON = LiteralChoiceTerminal(":")
val DOUBLE_COLON = LiteralChoiceTerminal("::")


val IPV6_FULL_SPECIFIED = SequenceCombinator(IPV6_HEXTET, COLON,  IPV6_HEXTET, COLON,  IPV6_HEXTET, COLON,  IPV6_HEXTET, COLON,  IPV6_HEXTET, COLON,  IPV6_HEXTET, COLON,  IPV6_HEXTET, COLON,  IPV6_HEXTET)
val IPV6_ZERO_HEXTET_BEFORE_ZERO_COMP = SequenceCombinator(DOUBLE_COLON, ZeroOrOne(SequenceCombinator(Repeat(SequenceCombinator(IPV6_HEXTET, COLON), 0, 8), IPV6_HEXTET)))
val IPV6_ONE_HEXTET_BEFORE_ZERO_COMP = SequenceCombinator(IPV6_HEXTET, DOUBLE_COLON, ZeroOrOne(SequenceCombinator( Repeat(SequenceCombinator(IPV6_HEXTET, COLON), 0, 7), IPV6_HEXTET)))
val IPV6_TWO_HEXTET_BEFORE_ZERO_COMP = SequenceCombinator(IPV6_HEXTET, COLON, IPV6_HEXTET, DOUBLE_COLON, ZeroOrOne(SequenceCombinator(Repeat(SequenceCombinator(IPV6_HEXTET, COLON), 0, 6), IPV6_HEXTET)))
val IPV6_THREE_HEXTET_BEFORE_ZERO_COMP = SequenceCombinator(IPV6_HEXTET, COLON,IPV6_HEXTET, COLON,IPV6_HEXTET, DOUBLE_COLON, ZeroOrOne(SequenceCombinator(Repeat(SequenceCombinator(IPV6_HEXTET, COLON), 0, 5), IPV6_HEXTET)))
val IPV6_FOUR_HEXTET_BEFORE_ZERO_COMP = SequenceCombinator(IPV6_HEXTET, COLON,IPV6_HEXTET, COLON,IPV6_HEXTET, COLON,IPV6_HEXTET, DOUBLE_COLON, ZeroOrOne(SequenceCombinator(Repeat(SequenceCombinator(IPV6_HEXTET, COLON), 0, 4), IPV6_HEXTET)))
val IPV6_FIVE_HEXTET_BEFORE_ZERO_COMP = SequenceCombinator(IPV6_HEXTET, COLON,IPV6_HEXTET, COLON,IPV6_HEXTET, COLON,IPV6_HEXTET, COLON,IPV6_HEXTET, DOUBLE_COLON, ZeroOrOne(SequenceCombinator(Repeat(SequenceCombinator(IPV6_HEXTET, COLON), 0, 3), IPV6_HEXTET)))
val IPV6_SIX_HEXTET_BEFORE_ZERO_COMP = SequenceCombinator(IPV6_HEXTET, COLON,IPV6_HEXTET, COLON,IPV6_HEXTET, COLON,IPV6_HEXTET, COLON,IPV6_HEXTET, COLON,IPV6_HEXTET, DOUBLE_COLON, ZeroOrOne(SequenceCombinator(Repeat(SequenceCombinator(IPV6_HEXTET, COLON), 0, 2), IPV6_HEXTET)))
val IPV6_SEVEN_HEXTET_BEFORE_ZERO_COMP = SequenceCombinator(IPV6_HEXTET, COLON,IPV6_HEXTET, COLON,IPV6_HEXTET, COLON,IPV6_HEXTET, COLON,IPV6_HEXTET, COLON,IPV6_HEXTET, COLON,IPV6_HEXTET, DOUBLE_COLON, ZeroOrOne(IPV6_HEXTET))


val IPV6_IPV4_SUFFIX_FULL = SequenceCombinator(IPV6_HEXTET, COLON,  IPV6_HEXTET, COLON,  IPV6_HEXTET, COLON,  IPV6_HEXTET, COLON,  IPV6_HEXTET, COLON,  IPV6_HEXTET, COLON,  IPV4_ADDR)
val IPV6_IPV4_SUFFIX_ZERO_HEXTET_BEFORE_ZERO_COMP =  SequenceCombinator(DOUBLE_COLON,SequenceCombinator(Repeat(SequenceCombinator(IPV6_HEXTET, COLON), 0, 6), IPV4_ADDR))
val IPV6_IPV4_SUFFIX_ONE_HEXTET_BEFORE_ZERO_COMP =  SequenceCombinator(IPV6_HEXTET, DOUBLE_COLON, SequenceCombinator(Repeat(SequenceCombinator(IPV6_HEXTET, COLON), 0, 5), IPV4_ADDR))
val IPV6_IPV4_SUFFIX_TWO_HEXTET_BEFORE_ZERO_COMP =  SequenceCombinator(IPV6_HEXTET, COLON, IPV6_HEXTET, DOUBLE_COLON, SequenceCombinator(Repeat(SequenceCombinator(IPV6_HEXTET, COLON), 0, 4), IPV4_ADDR))
val IPV6_IPV4_SUFFIX_THREE_HEXTET_BEFORE_ZERO_COMP =  SequenceCombinator(IPV6_HEXTET, COLON, IPV6_HEXTET, COLON, IPV6_HEXTET, DOUBLE_COLON,SequenceCombinator(Repeat(SequenceCombinator(IPV6_HEXTET, COLON), 0, 3), IPV4_ADDR))
val IPV6_IPV4_SUFFIX_FOUR_HEXTET_BEFORE_ZERO_COMP =  SequenceCombinator(IPV6_HEXTET, COLON, IPV6_HEXTET, COLON, IPV6_HEXTET, COLON, IPV6_HEXTET, DOUBLE_COLON, SequenceCombinator(Repeat(SequenceCombinator(IPV6_HEXTET, COLON), 0, 2), IPV4_ADDR))
val IPV6_IPV4_SUFFIX_FIVE_HEXTET_BEFORE_ZERO_COMP =  SequenceCombinator(IPV6_HEXTET, COLON, IPV6_HEXTET, COLON, IPV6_HEXTET, COLON, IPV6_HEXTET, COLON, IPV6_HEXTET, DOUBLE_COLON, IPV4_ADDR)

//val IPV6_ALL_ZEROS = DOUBLE_COLON

val IPV6_ADDR = Labeled(Role.LITERAL, tag = SemanticTag.IPV6, inner = AlternativeCombinator(
  IPV6_IPV4_SUFFIX_FULL,
  IPV6_IPV4_SUFFIX_ZERO_HEXTET_BEFORE_ZERO_COMP,
  IPV6_IPV4_SUFFIX_ONE_HEXTET_BEFORE_ZERO_COMP,
  IPV6_IPV4_SUFFIX_TWO_HEXTET_BEFORE_ZERO_COMP,
  IPV6_IPV4_SUFFIX_THREE_HEXTET_BEFORE_ZERO_COMP,
  IPV6_IPV4_SUFFIX_FOUR_HEXTET_BEFORE_ZERO_COMP,
  IPV6_IPV4_SUFFIX_FIVE_HEXTET_BEFORE_ZERO_COMP,
  IPV6_FULL_SPECIFIED,
  IPV6_SEVEN_HEXTET_BEFORE_ZERO_COMP,
  IPV6_SIX_HEXTET_BEFORE_ZERO_COMP,
  IPV6_FIVE_HEXTET_BEFORE_ZERO_COMP,
  IPV6_FOUR_HEXTET_BEFORE_ZERO_COMP,
  IPV6_THREE_HEXTET_BEFORE_ZERO_COMP,
  IPV6_TWO_HEXTET_BEFORE_ZERO_COMP,
  IPV6_ONE_HEXTET_BEFORE_ZERO_COMP,
  // Must go last because it's the most general and can match ::
  IPV6_ZERO_HEXTET_BEFORE_ZERO_COMP,

  // I suspect maybe that this one is redundant
  //IPV6_ALL_ZEROS,
))

val IPV6_ADDR_AND_PREFIX_LENGTH = SequenceCombinator(IPV6_ADDR, CIDR_SEPARATOR, IntegerTerminal(64, 129))
val IPV6_ADDR_AND_OPTIONAL_PREFIX_LENGTH = SequenceCombinator(IPV6_ADDR, ZeroOrOne(SequenceCombinator(CIDR_SEPARATOR, IntegerTerminal(64, 129))))


var IP_ADDR_AND_PREFIX_LENGTH = AlternativeCombinator(
  IPV4_ADDR_AND_OPTIONAL_PREFIX_LENGTH,
  IPV6_ADDR_AND_PREFIX_LENGTH)

var IN_ADDR_PREFIX_SPECIAL_VALUES = LiteralChoiceTerminal("any", "localhost", "link-local", "multicast")

var IPV4_ADDR_AND_PREFIX_OR_SPECIAL = AlternativeCombinator(
  IPV4_ADDR_AND_OPTIONAL_PREFIX_LENGTH,
  IN_ADDR_PREFIX_SPECIAL_VALUES,
)

var IPV6_ADDR_AND_PREFIX_OR_SPECIAL = AlternativeCombinator(
  IPV6_ADDR_AND_OPTIONAL_PREFIX_LENGTH,
  IN_ADDR_PREFIX_SPECIAL_VALUES,
)


var IP_ADDR_AND_PREFIX_OR_SPECIAL = AlternativeCombinator(
  IPV4_ADDR_AND_OPTIONAL_PREFIX_LENGTH,
  IPV6_ADDR_AND_OPTIONAL_PREFIX_LENGTH,
  IN_ADDR_PREFIX_SPECIAL_VALUES,
)

var IP_ADDR_PREFIX_LIST = SequenceCombinator(IP_ADDR_AND_PREFIX_OR_SPECIAL, ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), IP_ADDR_AND_PREFIX_OR_SPECIAL)))
var IPV4_ADDR_PREFIX_LIST = SequenceCombinator(IPV4_ADDR_AND_PREFIX_OR_SPECIAL, ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), IPV4_ADDR_AND_PREFIX_OR_SPECIAL)))
var IPV6_ADDR_PREFIX_LIST = SequenceCombinator(IPV6_ADDR_AND_PREFIX_OR_SPECIAL, ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), IPV6_ADDR_AND_PREFIX_OR_SPECIAL)))


// ---------------------------------------------------------------------------------------------------
// Hardware / MAC addresses (systemd src/basic/ether-addr-util.c). systemd's parse_hw_addr_full accepts
// three separator styles for the raw hex form:
//   colon / hyphen — 1-byte fields (1-2 hex digits each):   00:11:22:33:44:55  /  00-11-22-33-44-55
//   dot            — 2-byte fields (1-4 hex digits each):    0011.2233.4455
val HYPHEN = LiteralChoiceTerminal("-")
private val HW_ADDR_BYTE = RegexTerminal("[0-9a-fA-F]{1,2}", "[0-9a-fA-F]{1,2}")   // one 1-byte field
private val HW_ADDR_WORD = RegexTerminal("[0-9a-fA-F]{1,4}", "[0-9a-fA-F]{1,4}")   // one 2-byte field

// [count] groups of [group] separated by [sep]:  group (sep group){count-1}.
private fun hwAddrGroups(group: Combinator, sep: Combinator, count: Int): Combinator =
  SequenceCombinator(group, Repeat(SequenceCombinator(sep, group), count - 1, count - 1))

// A 6-byte Ethernet MAC address, i.e. systemd parse_ether_addr (expected_len == ETH_ALEN). Used by
// SR-IOV=, BridgeFDB=, DHCPServerStaticLease=, MACsec*=, NetDev/Peer= and MACVLAN/MACVTAP source.
val MAC_ADDRESS = Labeled(Role.LITERAL, AlternativeCombinator(
  hwAddrGroups(HW_ADDR_BYTE, COLON, 6),
  hwAddrGroups(HW_ADDR_BYTE, HYPHEN, 6),
  hwAddrGroups(HW_ADDR_WORD, DOT, 3),
))

// The raw hex forms of a hardware address (colon/hyphen use 1-byte fields → 4/6/16/20 groups; dot uses
// 2-byte fields → 2/3/8/10 groups). Longest group counts come first so the classic (first-full-match)
// matcher never stops on a shorter prefix. Wrapped once so the whole address reads as a single literal
// span rather than per-field.
private val RAW_HARDWARE_ADDRESS = Labeled(Role.LITERAL, AlternativeCombinator(
  hwAddrGroups(HW_ADDR_BYTE, COLON, 20), hwAddrGroups(HW_ADDR_BYTE, COLON, 16), hwAddrGroups(HW_ADDR_BYTE, COLON, 6), hwAddrGroups(HW_ADDR_BYTE, COLON, 4),
  hwAddrGroups(HW_ADDR_BYTE, HYPHEN, 20), hwAddrGroups(HW_ADDR_BYTE, HYPHEN, 16), hwAddrGroups(HW_ADDR_BYTE, HYPHEN, 6), hwAddrGroups(HW_ADDR_BYTE, HYPHEN, 4),
  hwAddrGroups(HW_ADDR_WORD, DOT, 10), hwAddrGroups(HW_ADDR_WORD, DOT, 8), hwAddrGroups(HW_ADDR_WORD, DOT, 3), hwAddrGroups(HW_ADDR_WORD, DOT, 2),
))

// A hardware address as accepted by parse_hw_addr_full with expected_len == 0 (Match=, Link=,
// Neighbor.LinkLayerAddress=). Besides 6-byte MACs it accepts 4-, 16- and 20-byte (Infiniband)
// hardware addresses, and — because the parser first tries in_addr_from_string — plain IPv4 and IPv6
// address literals. IPV4_ADDR / IPV6_ADDR are already Labeled (IPv6 also carries SemanticTag.IPV6), so
// they sit alongside the wrapped raw forms rather than inside a second wrapper — a whole-alternation
// Labeled would emit a redundant second literal region over an IP literal's span.
val HARDWARE_ADDRESS = AlternativeCombinator(
  IPV6_ADDR,
  IPV4_ADDR,
  RAW_HARDWARE_ADDRESS,
)


// ---------------------------------------------------------------------------------------------------
// Condition*= / Assert*= (systemd src/core/load-fragment.c). Both parsers strip an optional leading
// trigger marker `|` and then an optional negation marker `!`, in that order, before handing what is
// left to the per-condition check. Only that order is recognised: in `!|foo` the `!` negates and the
// parameter is literally `| foo`, which no condition accepts.
val PIPE = LiteralChoiceTerminal("|")
val BANG = LiteralChoiceTerminal("!")

// Both helpers below spell the marker combinations out as alternatives rather than wrapping each
// marker in a ZeroOrOne. ZeroOrOne probes its inner combinator a second time to report how far input
// could reach, so `!!/some/path` would report a longest-match one character past the `!` the grammar
// actually consumed, and the classic engine derives its error TextRange from that number — landing it
// past the end of the value. Alternatives keep the reported offset equal to what was consumed.

/**
 * A `Condition<Path>=`/`Assert<Path>=` value.
 *
 * config_parse_unit_condition_path advances with a bare `rvalue++` per marker, so no whitespace may
 * follow either one.
 */
fun conditionPath(parameter: Combinator): Combinator = SequenceCombinator(
  AlternativeCombinator(
    SequenceCombinator(LiteralChoiceTerminal("|!"), parameter),
    SequenceCombinator(PIPE, parameter),
    SequenceCombinator(BANG, parameter),
    parameter,
  ),
  EOF()
)

/**
 * A `Condition<Name>=`/`Assert<Name>=` value.
 *
 * config_parse_unit_condition_string advances with `rvalue += 1 + strspn(rvalue + 1, WHITESPACE)`, so
 * whitespace after a marker is skipped.
 */
fun conditionString(parameter: Combinator): Combinator {
  val optionalWhitespace = ZeroOrOne(WhitespaceTerminal())
  return SequenceCombinator(
    AlternativeCombinator(
      SequenceCombinator(PIPE, optionalWhitespace, BANG, optionalWhitespace, parameter),
      SequenceCombinator(PIPE, optionalWhitespace, parameter),
      SequenceCombinator(BANG, optionalWhitespace, parameter),
      parameter,
    ),
    EOF()
  )
}

// ---------------------------------------------------------------------------------------------------
// Paths that systemd requires to be absolute — anything it feeds through unit_path_printf() and then
// path_simplify_and_warn(..., PATH_CHECK_ABSOLUTE).
//
// That helper (src/shared/parse-helpers.c) checks, in order: valid UTF-8, absolute, then — after
// path_simplify() has collapsed `//`, `./` and any trailing slash — a length under PATH_MAX and
// path_is_normalized(), which is what rejects a surviving `..` component. Nothing restricts which
// characters a component may contain, so a path may perfectly well contain spaces.
//
// Whether a space *ends* the value depends on the caller, not on the path rules, which is why there
// are two terminals below:
//
//   UNIT_PATH           the setting takes one path and hands the parser the whole rvalue verbatim —
//                       no word splitting, no unquoting, no unescaping. Condition*=/Assert*= and the
//                       [Path] watch settings work this way, so `/mnt/My Data` is a single path and a
//                       backslash in it is an ordinary character.
//   QUOTABLE_UNIT_PATH  the setting takes a list and splits it with extract_first_word(). That drops
//                       backslashes and, with EXTRACT_UNQUOTE, honours '…' and "…", so a path with a
//                       space has to be escaped or quoted to survive splitting.
//
// Specifiers are resolved before the absolute check, so a value may legitimately begin with one
// (`%t/foo`, `%h/.cache`) rather than with a slash.

private const val PATH_START = """(?:/|%\S)"""

// Rejects a `..` component anywhere: the optional `(?:[\s\S]*/)` swallows any leading directories, so
// the lookahead fires on `/a/../b` and `/a/..` but not on `/a/..b` or `/a/b..`.
private const val NO_DOT_DOT = """(?!(?:[\s\S]*/)?\.\.(?:/|$))"""

/** One whole-value absolute path: everything from here to the end of the value belongs to it. */
val UNIT_PATH = RegexTerminal(
  """[\s\S]+""",
  """$NO_DOT_DOT$PATH_START[\s\S]*"""
)

/** One element of a whitespace-separated path list, optionally quoted or backslash-escaped. */
val QUOTABLE_UNIT_PATH = RegexTerminal(
  """"[^"]*"|'[^']*'|(?:[^\s\\]|\\[\s\S])+""",
  """"$PATH_START(?:[^"\\]|\\[\s\S])*"|'$PATH_START(?:[^'\\]|\\[\s\S])*'|$PATH_START(?:[^\s\\]|\\[\s\S])*"""
)


// ---------------------------------------------------------------------------------------------------
// IP addresses and prefixes, as parsed by in_addr_from_string_auto / in_addr_prefix_from_string_auto
// (systemd src/basic/in-addr-util.c). IPv6 is tried first throughout: the keyword and IPv4 branches
// can consume a leading fragment of an IPv6 literal, and the classic matcher never backtracks into a
// sibling alternative once one of them has matched.

/** A bare address literal of either family — in_addr_from_string_auto. */
val IP_ADDR = AlternativeCombinator(IPV6_ADDR, IPV4_ADDR)

// in_addr_prefix_from_string_auto accepts the full prefix-length range the family allows, and treats
// the length as optional (defaulting to the full width). This is deliberately wider than
// IPV4_ADDR_AND_OPTIONAL_PREFIX_LENGTH / IPV6_ADDR_AND_OPTIONAL_PREFIX_LENGTH above, which model the
// narrower ranges systemd enforces for InAddrPrefixes-style settings.
val IPV4_ADDR_AND_ANY_PREFIX = SequenceCombinator(IPV4_ADDR, ZeroOrOne(SequenceCombinator(CIDR_SEPARATOR, IntegerTerminal(0, 33))))
val IPV6_ADDR_AND_ANY_PREFIX = SequenceCombinator(IPV6_ADDR, ZeroOrOne(SequenceCombinator(CIDR_SEPARATOR, IntegerTerminal(0, 129))))
val IP_ADDR_AND_ANY_PREFIX = AlternativeCombinator(IPV6_ADDR_AND_ANY_PREFIX, IPV4_ADDR_AND_ANY_PREFIX)


// ---------------------------------------------------------------------------------------------------
// Network interface names — ifname_valid_full (systemd src/basic/socket-util.c). Valid characters are
// printable ASCII (33…126) except `:`, `/` and `%`. A name that is entirely digits is refused so it
// can't be confused with an interface index, as are `.`, `..`, and — because they collide with the
// /proc/sys/net/*/conf/ directories — `all` and `default`. The length limit differs by call site.
private const val IFNAME_CHAR = """[\x21-\x7E&&[^:/%]]"""

// The lookahead rejects the reserved words only when they make up the whole name: `(?!IFNAME_CHAR)`
// after each one means "and the name ends here", so `1a`, `alliance` and `defaults` still pass.
private fun ifnameSemantic(maxLength: Int) =
  """(?!(?:[0-9]+|\.\.?|all|default)(?!$IFNAME_CHAR))$IFNAME_CHAR{1,$maxLength}"""

/** ifname_valid_full with no flags: at most IFNAMSIZ - 1 = 15 characters. */
val INTERFACE_NAME = RegexTerminal("""\S+""", ifnameSemantic(15))

/** ifname_valid_full with IFNAME_VALID_ALTERNATIVE: at most ALTIFNAMSIZ - 1 = 127 characters. */
val ALTERNATIVE_INTERFACE_NAME = RegexTerminal("""\S+""", ifnameSemantic(127))


/**
 * An unsigned number in `[minInclusive, maxExclusive)`, spelled any of the ways systemd's `safe_atou*`
 * family reads one — decimal, `0x` hexadecimal or leading-zero octal, since those helpers pass base 0
 * to strtoul(). See [UnsignedNumberTerminal], which parses in the actual base so the bounds hold for
 * every spelling.
 */
fun unsignedNumber(maxExclusive: Long, minInclusive: Long = 0L): Combinator =
  UnsignedNumberTerminal(minInclusive, maxExclusive)



// ---------------------------------------------------------------------------------------------------
// Capability names — capability_from_name (systemd src/basic/capability-list.c).
//
// The lookup table is gperf-generated with `--ignore-case` (src/basic/meson.build), and the reverse
// mapping capability_to_name() renders names in lower case (src/basic/capability-to-name.awk uses
// `tolower`), so both `CAP_SYS_ADMIN` and `cap_sys_admin` resolve. The upper-case
// FlexibleLiteralChoiceTerminal is kept as the first alternative because it is what supplies the
// quick-fix suggestions on a misspelled name; the regex behind it accepts any other casing.
//
// The name list is read back off that terminal rather than duplicated, so the two can't drift.
val CAPABILITY_NAME = FlexibleLiteralChoiceTerminal(
  *SimpleGrammarOptionValues.Capabilities.choices,
  ignoreCase = true,
)
