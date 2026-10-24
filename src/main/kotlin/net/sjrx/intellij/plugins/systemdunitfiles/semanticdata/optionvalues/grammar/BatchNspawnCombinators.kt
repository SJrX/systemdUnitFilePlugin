package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

/*
 * Shared value grammars for the .nspawn settings parsers (systemd src/nspawn/nspawn-settings.c and the
 * helpers it calls in nspawn-mount.c / nspawn-network.c / nspawn-expose-ports.c).
 *
 * Everything here is pinned to systemd 0f0682b773, the commit recorded in
 * systemd-build/build/last_commit_hash. Browse it at https://github.com/systemd/systemd/blob/0f0682b773.
 *
 * Several of these are general-purpose (the colon-delimited interface name, the address-family list)
 * and are candidates for promotion into Combinators.kt; they live here only so this batch does not
 * collide with parallel edits there. Booleans use the shared [BOOLEAN], and numbers the shared
 * [SafeAtoTerminal].
 */

// ---------------------------------------------------------------------------------------------------
// Interface names — ifname_valid() (ifname_valid_full with no flags, src/basic/socket-util.c):
// printable ASCII 33…126 except `:`, `/` and `%`, at most IFNAMSIZ-1 = 15 bytes, not `.`/`..`, not
// `all`/`default`, not all digits, and not anything parse_ifindex() accepts — i.e. anything safe_atoi()
// reads as a positive `int`, such as `0x10`, `+5`, `0o7` or `0b1`. (`0x0`, `-5` or an out-of-range
// `0xFFFFFFFFFF` are not ifindexes and so are valid names.)
//
// Unlike [INTERFACE_NAME] the syntactic form stops at `:` as well as whitespace, because every nspawn
// caller splits `NAME:NAME` pairs on the colon.
//
// Backslashes: Interface=/MACVLAN=/IPVLAN= unescape each word twice — once in the outer
// extract_first_word(WHITESPACE, 0) and again in the extract_first_word(":", 0) that splits the pair —
// and VirtualEthernetExtra= once. A name that runs into whitespace after a run of n backslashes is
// therefore invalid unless n is a multiple of 4: an odd n escapes the whitespace into the name (`a\ b`,
// "interface name not valid: a b"), and n = 2 (mod 4) leaves a lone trailing backslash for the second
// pass (`a\\ b`, "Failed to extract first word"). [nspawnIfnameEscapesWhitespace] models exactly that;
// both verdicts were confirmed with fuzz-nspawn-settings at 0f0682b773. Still not modelled: an escaping
// backslash elsewhere is counted towards the 15-byte limit here although C drops it.

private const val NSPAWN_IFNAME_CHAR = """[\x21-\x7E&&[^:/%]]"""

private fun nspawnIfnameSemantic(maxLength: Int) =
  """(?!(?:[0-9]+|\.\.?|all|default)(?!$NSPAWN_IFNAME_CHAR))$NSPAWN_IFNAME_CHAR{1,$maxLength}"""

/**
 * A [RegexTerminal] for interface names that additionally refuses, as ifname_valid() does, a span that
 * parse_ifindex() would read as an interface index.
 */
private class NspawnInterfaceNameTerminal : TerminalCombinator {
  private val regex = RegexTerminal("""[^\s:]+""", nspawnIfnameSemantic(15))

  override fun SyntacticMatch(value: String, offset: Int): MatchResult = regex.SyntacticMatch(value, offset).owned()

  override fun SemanticMatch(value: String, offset: Int): MatchResult {
    val result = regex.SemanticMatch(value, offset)
    if (result.matchResult >= 0 && isRefused(value, offset, result.matchResult)) {
      return NoMatch.copy(longestMatch = offset)
    }
    return result.owned()
  }

  override fun parse(value: String, offset: Int): Sequence<ParseStep> = regex.parse(value, offset).map { step ->
    when (step) {
      is Parse -> step.copy(tokens = step.tokens.map { it.copy(terminal = this, valid = it.valid && !isRefused(value, it.start, it.end)) })
      is Stuck -> Stuck(step.offset, setOf(this))
    }
  }

  private fun isRefused(value: String, start: Int, end: Int): Boolean =
    ifindex.valueOf(value.substring(start, end)) != null || nspawnIfnameEscapesWhitespace(value, start, end)

  /** parse_ifindex(): safe_atoi() on the whole name, then refuses anything not greater than zero. */
  private val ifindex = SafeAtoTerminal(
    signed = true,
    base = 0,
    flags = emptySet(),
    minInclusive = 1L,
    maxInclusive = Int.MAX_VALUE.toLong(),
    acceptsWhitespace = true,
    acceptsMinus = true,
  )

  /** Report this terminal, not the wrapped regex, as the one that matched. */
  private fun MatchResult.owned(): MatchResult = if (matchResult < 0) this else copy(terminals = listOf(this@NspawnInterfaceNameTerminal))

  override fun toString(): String = "NspawnIfname"
}

/**
 * Whether the name `value[start, end)` ends in a run of backslashes that is not a multiple of 4 long and is
 * followed by whitespace — see the comment above [NSPAWN_IFNAME_CHAR] for why C refuses that.
 */
private fun nspawnIfnameEscapesWhitespace(value: String, start: Int, end: Int): Boolean {
  if (end >= value.length || !value[end].isWhitespace()) return false
  var run = 0
  while (end - run - 1 >= start && value[end - run - 1] == '\\') run++
  return run % 4 != 0
}

/** One interface name in a `NAME[:NAME]` pair. */
val COLON_DELIMITED_INTERFACE_NAME: TerminalCombinator = NspawnInterfaceNameTerminal()


// ---------------------------------------------------------------------------------------------------
// Colon-separated path fields, as split by extract_first_word(..., ":", EXTRACT_DONT_COALESCE_SEPARATORS)
// (src/basic/extract-word.c). With no EXTRACT_UNQUOTE/EXTRACT_CUNESCAPE flag a backslash simply makes
// the next character literal (so `\:` embeds a colon) and quotes are ordinary characters; a trailing lone
// backslash is an error. Whitespace is NOT a separator here, so a path may contain spaces.

/** The characters one colon-delimited field may contain. */
internal const val COLON_FIELD_CHAR = """(?:[^:\\]|\\[\s\S])"""

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
