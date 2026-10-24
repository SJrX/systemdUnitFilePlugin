package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

/*
 * Shared value grammars for the .nspawn settings parsers (systemd src/nspawn/nspawn-settings.c and the
 * helpers it calls in nspawn-mount.c / nspawn-network.c / nspawn-expose-ports.c).
 *
 * Everything here is pinned to systemd 0f0682b773, the commit recorded in
 * systemd-build/build/last_commit_hash. Browse it at https://github.com/systemd/systemd/blob/0f0682b773.
 *
 * Several of these are general-purpose (the safe_ato* number terminal, the case-insensitive boolean,
 * the colon-delimited interface name, the address-family list) and are candidates for promotion into
 * Combinators.kt; they live here only so this batch does not collide with parallel edits there.
 */

// ---------------------------------------------------------------------------------------------------
// Numbers as systemd's safe_atoi()/safe_atou*() read them (src/basic/parse-util.c).
//
// Both helpers call mangle_base() first, which recognises the Python-style `0b`/`0B` (binary) and
// `0o`/`0O` (octal) prefixes, and then strtol()/strtoul() with base 0, which adds C's `0x` hex and
// leading-zero octal plus an optional `+`/`-` sign. mangle_base() only looks at the very start of the
// string, so a sign in front of `0b`/`0o` defeats it and strtol() then stops at the `b`/`o` — such
// spellings are rejected, and the shape below does not allow them.

/**
 * An integer in `[minInclusive, maxExclusive)` spelled any way safe_atoi()/safe_atou*() accept:
 * decimal, `0x` hex, leading-zero octal, `0o` octal or `0b` binary, with an optional sign on the
 * decimal/hex/octal forms. The range check is done on the real value, so it holds however the number
 * is spelled.
 *
 * A negative value is only in range when [minInclusive] allows it. safe_atou() accepts `-0` (it only
 * refuses a minus sign on a non-zero result) and so does this terminal, by treating it as zero.
 *
 * Unlike [UnsignedNumberTerminal] this also understands the `0b`/`0o` prefixes and signs.
 */
class SafeAtoNumberTerminal(
  private val minInclusive: Long,
  private val maxExclusive: Long,
) : TerminalCombinator {

  private fun span(value: String, offset: Int): String? = SHAPE.matchAt(value, offset)?.value

  /** The numeric value of [text], or null when it does not fit a Long (and so is out of range anyway). */
  private fun valueOf(text: String): Long? {
    if (text.length > 2 && (text.startsWith("0b") || text.startsWith("0B"))) return text.drop(2).toLongOrNull(2)
    if (text.length > 2 && (text.startsWith("0o") || text.startsWith("0O"))) return text.drop(2).toLongOrNull(8)
    val negative = text.startsWith("-")
    val body = text.removePrefix("-").removePrefix("+")
    val magnitude = when {
      body.length > 2 && (body.startsWith("0x") || body.startsWith("0X")) -> body.drop(2).toLongOrNull(16)
      body.length > 1 && body.startsWith("0") -> body.drop(1).toLongOrNull(8)
      else -> body.toLongOrNull()
    } ?: return null
    return if (negative) -magnitude else magnitude
  }

  private fun inRange(text: String): Boolean = valueOf(text)?.let { it >= minInclusive && it < maxExclusive } ?: false

  override fun SyntacticMatch(value: String, offset: Int): MatchResult {
    val text = span(value, offset) ?: return NoMatch
    return MatchResult(listOf(text), offset + text.length, listOf(this), offset + text.length)
  }

  override fun SemanticMatch(value: String, offset: Int): MatchResult {
    val text = span(value, offset) ?: return NoMatch
    if (!inRange(text)) return NoMatch.copy(longestMatch = offset)
    return MatchResult(listOf(text), offset + text.length, listOf(this), offset + text.length)
  }

  override fun parse(value: String, offset: Int): Sequence<ParseStep> {
    val text = span(value, offset) ?: return sequenceOf(Stuck(offset, setOf(this)))
    return sequenceOf(Parse(offset + text.length, listOf(ParsedToken(offset, offset + text.length, text, this, inRange(text)))))
  }

  override fun toString(): String = "SafeAto($minInclusive,$maxExclusive)"

  private companion object {
    // The prefixed forms come first so `0b1`/`0x1f` are not read as a lone octal `0`.
    val SHAPE = Regex("""0[bB][01]+|0[oO][0-7]+|[+-]?(?:0[xX][0-9a-fA-F]+|0[0-7]*|[1-9][0-9]*)""")
  }
}

/** A [SafeAtoNumberTerminal] coloured as a literal, like the other number terminals. */
fun safeAtoNumber(minInclusive: Long, maxExclusive: Long): Combinator =
  Labeled(Role.LITERAL, SafeAtoNumberTerminal(minInclusive, maxExclusive))


// ---------------------------------------------------------------------------------------------------
// Booleans — parse_boolean() (src/basic/parse-util.c) compares with STRCASE_IN_SET, so `Yes`, `TRUE`
// and `Off` are as good as their lower-case spellings. The shared [BOOLEAN] is case-sensitive; this
// one is not.

/** parse_boolean(), case-insensitively. */
val CASE_INSENSITIVE_BOOLEAN = FlexibleLiteralChoiceTerminal(*BOOLEAN_SPELLINGS, ignoreCase = true)

/**
 * A DEFINE_STRING_TABLE_LOOKUP_WITH_BOOLEAN enum: string_table_lookup_from_string_with_boolean()
 * (src/basic/string-table.c) tries parse_boolean() first and then the table with a case-sensitive
 * streq(). The names come first so that [CASE_INSENSITIVE_BOOLEAN] cannot strand part of a name.
 */
fun enumWithBoolean(vararg names: String): Combinator =
  SequenceCombinator(AlternativeCombinator(FlexibleLiteralChoiceTerminal(*names), CASE_INSENSITIVE_BOOLEAN), EOF())


// ---------------------------------------------------------------------------------------------------
// Interface names — ifname_valid() (ifname_valid_full with no flags, src/basic/socket-util.c):
// printable ASCII 33…126 except `:`, `/` and `%`, at most IFNAMSIZ-1 = 15 bytes, not `.`/`..`, not
// `all`/`default`, and not all digits. (parse_ifindex() also refuses anything safe_atoi() reads as a
// positive number, e.g. `0x10` or `+5`; those rare spellings are not modelled and are accepted.)
//
// Unlike [INTERFACE_NAME] the syntactic form stops at `:` as well as whitespace, because every nspawn
// caller splits `NAME:NAME` pairs on the colon.

private const val NSPAWN_IFNAME_CHAR = """[\x21-\x7E&&[^:/%]]"""

private fun nspawnIfnameSemantic(maxLength: Int) =
  """(?!(?:[0-9]+|\.\.?|all|default)(?!$NSPAWN_IFNAME_CHAR))$NSPAWN_IFNAME_CHAR{1,$maxLength}"""

/** One interface name in a `NAME[:NAME]` pair. */
val COLON_DELIMITED_INTERFACE_NAME = RegexTerminal("""[^\s:]+""", nspawnIfnameSemantic(15))


// ---------------------------------------------------------------------------------------------------
// Colon-separated path fields, as split by extract_first_word(..., ":", EXTRACT_DONT_COALESCE_SEPARATORS)
// (src/basic/extract-word.c). With no EXTRACT_UNQUOTE/EXTRACT_CUNESCAPE flag a backslash simply makes
// the next character literal (so `\:` embeds a colon) and quotes are ordinary characters; a trailing lone
// backslash is an error. Whitespace is NOT a separator here, so a path may contain spaces.

/** The characters one colon-delimited field may contain. */
private const val COLON_FIELD_CHAR = """(?:[^:\\]|\\[\s\S])"""

/** Any non-empty colon-delimited field — the lenient (syntactic) shape shared by the terminals below. */
private const val COLON_FIELD = """$COLON_FIELD_CHAR+"""

/** A colon-delimited field that must be an absolute path: path_is_absolute() only checks for a leading `/`. */
val COLON_FIELD_ABSOLUTE_PATH = RegexTerminal(COLON_FIELD, """\\?/$COLON_FIELD_CHAR*""")

/**
 * A colon-delimited nspawn *source* path — source_path_parse() (src/nspawn/nspawn-mount.c): non-empty;
 * either `+` followed by an absolute path (relative to the container image) or any other path, which
 * is made absolute against the working directory, so relative paths are fine.
 */
val COLON_FIELD_SOURCE_PATH = RegexTerminal(COLON_FIELD, """\+\\?/$COLON_FIELD_CHAR*|(?!\+)$COLON_FIELD_CHAR+""")

/**
 * A colon-delimited field that must be absolute *and* normalized — path_is_absolute() &&
 * path_is_normalized() (src/basic/path-util.c): no empty (`//`), `.` or `..` component. A trailing
 * slash is fine. The component length limit (NAME_MAX) is not modelled.
 */
val COLON_FIELD_NORMALIZED_PATH = RegexTerminal(COLON_FIELD, normalizedAbsolutePath("""[^/:\\]|\\[\s\S]""", fieldEnd = """:|$"""))

/**
 * The rest of the value as an absolute, normalized path, colons and backslashes included verbatim —
 * for the trailing field that a parser strdup()s instead of extracting (PivotRoot='s second path).
 */
val REST_NORMALIZED_PATH = RegexTerminal("""[\s\S]+""", normalizedAbsolutePath("""[^/]""", fieldEnd = """$"""))

/**
 * `/`, or one or more `/component`s with an optional trailing slash, where no component is `.`/`..`.
 * [fieldEnd] is what may follow the path (the end of the value, or also a `:` separator).
 */
private fun normalizedAbsolutePath(componentChar: String, fieldEnd: String): String {
  val component = """(?!\.\.?(?:/|$fieldEnd))(?:$componentChar)+"""
  return """(?:/$component)+/?|/"""
}


// ---------------------------------------------------------------------------------------------------
// User names — valid_user_group_name(name, 0), i.e. the STRICT flavour (src/basic/user-util.c): a
// letter or underscore, then letters, digits, `_` or `-`, at most 31 bytes in total (the utmp ut_user
// field). No dots, no leading digit or dash, no numeric UIDs.

/** One whitespace-separated user name in strict valid_user_group_name() form. */
val STRICT_USER_NAME = RegexTerminal("""\S+""", """[A-Za-z_][A-Za-z0-9_-]{0,30}(?![A-Za-z0-9_-])""")


// ---------------------------------------------------------------------------------------------------
// 128-bit IDs — sd_id128_from_string() (src/libsystemd/sd-id128/sd-id128.c): 32 hex digits, or the
// 8-4-4-4-12 UUID form, optionally behind a case-insensitive `urn:uuid:` prefix, which then requires the
// UUID form. Hex digits are case-insensitive.

private const val ID128_PLAIN = """[0-9a-fA-F]{32}"""
private const val ID128_UUID = """[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"""

// id128_from_string_nonzero() additionally refuses the all-zero ID.
private const val ID128_NONZERO = """(?=[0-]*[1-9a-fA-F])"""

/** The `urn:uuid:` prefix sd_id128_from_string() strips with startswith_no_case(). */
val ID128_URN_PREFIX = RegexTerminal("""(?i)urn:uuid:""", """(?i)urn:uuid:""")

/** A non-zero 128-bit ID in UUID (dashed) form only. */
val NONZERO_ID128_UUID = RegexTerminal("""[0-9A-Za-z-]+""", """$ID128_NONZERO$ID128_UUID""")

/** A non-zero 128-bit ID in either the plain 32-hex-digit or the UUID form. */
val NONZERO_ID128 = RegexTerminal("""[0-9A-Za-z-]+""", """$ID128_NONZERO(?:$ID128_PLAIN|$ID128_UUID)""")


// ---------------------------------------------------------------------------------------------------
// Address families — af_from_name() (src/basic/af-list.c). The lookup table is gperf-generated with
// `--ignore-case` (src/basic/meson.build), and af_from_name() retries a failed lookup with `AF_`
// prepended, so `AF_INET`, `af_inet`, `inet` and `INET6` all resolve.
//
// The canonical names are those of ConfigParseAddressFamiliesOptionValue (the AF_* macros in
// <sys/socket.h>, minus AF_UNSPEC and AF_MAX).
private val ADDRESS_FAMILY_CANONICAL_NAMES = arrayOf(
  "AF_ALG", "AF_APPLETALK", "AF_ASH", "AF_ATMPVC", "AF_ATMSVC", "AF_AX25", "AF_BLUETOOTH", "AF_BRIDGE",
  "AF_CAIF", "AF_CAN", "AF_DECnet", "AF_ECONET", "AF_FILE", "AF_IB", "AF_IEEE802154", "AF_INET",
  "AF_INET6", "AF_IPX", "AF_IRDA", "AF_ISDN", "AF_IUCV", "AF_KCM", "AF_KEY", "AF_LLC", "AF_LOCAL",
  "AF_MCTP", "AF_MPLS", "AF_NETBEUI", "AF_NETLINK", "AF_NETROM", "AF_NFC", "AF_PACKET", "AF_PHONET",
  "AF_PPPOX", "AF_QIPCRTR", "AF_RDS", "AF_ROSE", "AF_ROUTE", "AF_RXRPC", "AF_SECURITY", "AF_SMC",
  "AF_SNA", "AF_TIPC", "AF_UNIX", "AF_VSOCK", "AF_WANPIPE", "AF_X25", "AF_XDP",
)

private val REMOVED_ADDRESS_FAMILIES = mapOf(
  "AF_DECnet" to "AF_DECnet is obsolete: DECnet support was removed from the Linux kernel in 6.1.",
  "AF_IRDA" to "AF_IRDA is obsolete: IrDA support was removed from the Linux kernel in 4.17.",
  "AF_ECONET" to "AF_ECONET is obsolete: Acorn Econet support was removed from the Linux kernel in 3.5.",
  "AF_WANPIPE" to "AF_WANPIPE is obsolete: WANPIPE support was removed from the Linux kernel in 2.6.21.",
)

/** An address family name, with or without the `AF_` prefix, in any case. */
val ADDRESS_FAMILY_NAME = FlexibleLiteralChoiceTerminal(
  *ADDRESS_FAMILY_CANONICAL_NAMES,
  *ADDRESS_FAMILY_CANONICAL_NAMES.map { it.removePrefix("AF_") }.toTypedArray(),
  ignoreCase = true,
).deprecating(REMOVED_ADDRESS_FAMILIES + REMOVED_ADDRESS_FAMILIES.mapKeys { it.key.removePrefix("AF_") })
