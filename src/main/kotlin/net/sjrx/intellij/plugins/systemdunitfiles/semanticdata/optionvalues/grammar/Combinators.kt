package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

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


