package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

/*
 * Shared value grammars for the ExecContext sandboxing / image / credential / environment settings in
 * systemd src/core/load-fragment.c (exec batch, 2026-10). Everything here is pinned to systemd
 * 0f0682b773, the commit recorded in systemd-build/build/last_commit_hash.
 *
 * words  https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188   extract_first_word
 * escape https://github.com/systemd/systemd/blob/0f0682b773/src/basic/escape.c#L104             cunescape_one
 * path   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L34      path_simplify_and_warn
 * b64    https://github.com/systemd/systemd/blob/0f0682b773/src/basic/hexdecoct.c#L755          unbase64mem_full
 * uint   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429         safe_atou_full (+ mangle_base L403)
 *
 * The grammar sees the RAW value text, before systemd unquotes or unescapes anything, so the terminals
 * below model the raw spelling of a word. Most of these settings split their value twice:
 *
 *  1. into whitespace-separated words with extract_first_word(…, EXTRACT_UNQUOTE|EXTRACT_RETAIN_ESCAPE)
 *     — quotes are removed (and may appear anywhere in a word), backslashes are kept verbatim;
 *  2. each word into ':'-separated fields with EXTRACT_CUNESCAPE|EXTRACT_UNESCAPE_SEPARATORS — so a
 *     backslash must start a valid C escape (cunescape_one) or escape the ':' separator (`\:`).
 *
 * Deliberate approximations, shared by every grammar built from these pieces:
 *  - a quoted run ("…" or '…') is treated as opaque field text. systemd unquotes BEFORE splitting on
 *    ':', so a ':' inside quotes is really a separator. The grammar keeps it inside the field, which is
 *    usually lenient, but NOT always: when such a run is followed by more ':'-fields outside the quotes
 *    (`BindPaths="/a:/b":rbind`, `MountImages="/a.raw:/mnt":ro`), or a literal field such as rbind or
 *    a partition designator is itself quoted (`BindPaths=/a:/b:"rbind"`, `RootImageOptions="root":ro`),
 *    the field count shifts and a value systemd accepts is flagged. None of these spellings occurs in
 *    the corpus; modelling them needs a combinator that matches against the unquoted text.
 *    Escapes inside quotes are not checked.
 *  - under EXTRACT_RETAIN_ESCAPE a backslash is not special in the outer split, so a raw `\"` keeps the
 *    backslash and then opens a quote. The grammar therefore does not treat `\"` / `\'` as escapes
 *    outside quotes (an unbalanced quote is refused by systemd, and is flagged here too).
 *  - '%' specifiers are accepted wherever a path may start, without checking that the specifier exists
 *    or that it expands to an absolute path.
 */

// `\uXXXX`: not NUL, not a UTF-16 surrogate (D800-DFFF). Noncharacters such as FFFE are deliberately allowed.
private const val ESCAPE_U16 = """u(?!0000)(?![dD][89abAB])[0-9a-fA-F]{4}"""

// `\UXXXXXXXX`: not NUL, and unichar_is_valid(): below 0x110000, not a surrogate, not FDD0-FDEF, and the
// low 16 bits not FFFE/FFFF.
private const val ESCAPE_U32 = "U(?!0{8})(?=00(?:0[0-9a-fA-F]|10)[0-9a-fA-F]{4})(?!0000[dD][89abAB])" +
  "(?!0000[fF][dD][dDeE])(?![0-9a-fA-F]{4}[fF]{3}[eEfF])[0-9a-fA-F]{8}"

private const val C_ESCAPE_COMMON = """x(?!00)[0-9a-fA-F]{2}|$ESCAPE_U16|$ESCAPE_U32|(?!000)[0-3][0-7]{2}"""

/**
 * A valid C escape for cunescape_one, minus the leading backslash. NUL bytes, octal > 0377, UTF-16
 * surrogates in `\u` and invalid code points in `\U` (unichar_is_valid) are refused.
 */
internal const val C_ESCAPE_BODY = """(?:[abfnrtv\\"'s]|$C_ESCAPE_COMMON)"""

// As C_ESCAPE_BODY but without `\"` / `\'`, which under EXTRACT_RETAIN_ESCAPE are not escapes in the outer
// split (the backslash is kept and the quote opens a quoted run).
private const val RETAINED_ESCAPE_BODY = """(?:[abfnrtv\\s]|$C_ESCAPE_COMMON)"""

// ---------------------------------------------------------------------------------------------------
// ':'-separated fields of a word that was split with EXTRACT_RETAIN_ESCAPE and is then re-split with
// EXTRACT_CUNESCAPE|EXTRACT_UNESCAPE_SEPARATORS (BindPaths=, MountImages=, ExtensionImages=,
// RootImageOptions=).

// Lenient shape: any run of non-blank, non-':' characters, quoted runs, and backslash + non-blank.
// A backslash followed by whitespace is never valid here: the outer split keeps the backslash and cuts
// the word at the blank, leaving a trailing backslash that the inner split refuses with -EINVAL.
private const val RETAINED_FIELD_SYNTAX = """(?:[^\s:"'\\]|"[^"]*"|'[^']*'|\\[^\s"']|\\(?=["']))+"""

// Strict body: as above, but a backslash must start a C escape or escape ':' itself. A backslash before a
// quote is kept verbatim by the outer split and the quote then opens a quoted run, which must be closed;
// the escape the backslash forms with the run's first character is not checked.
private const val RETAINED_FIELD_BODY = """(?:[^\s:"'\\]|"[^"]*"|'[^']*'|\\(?:$RETAINED_ESCAPE_BODY|:|(?=["'])))+"""

// The unquoted field must start with '/' (absolute) or a specifier. Quotes are skipped over first.
private const val ABSOLUTE_HEAD = """(?=["']*[/%])"""

// path_is_normalized() refuses a surviving ".." component (path_simplify() has already removed "." and
// "//"). The optional `[^\s:]*/` swallows leading directories so "/a/../b" and "/a/.." are caught but
// "/a/..b" and "/a/b.." are not.
private const val NO_DOT_DOT_COMPONENT = """(?!(?:[^\s:]*/)?\.\.(?:[/\s:"']|$))"""

// path_below_api_vfs(): "/proc", "/sys" and (unless DEV_OK) "/dev", as whole leading components.
private const val NOT_PROC_OR_SYS = """(?!["']*-?["']*/+(?:proc|sys)(?:[/\s:"']|$))"""
private const val NOT_API_VFS = """(?!["']*-?["']*/+(?:proc|sys|dev)(?:[/\s:"']|$))"""

// Every semantic regex in this file ends with a lookahead that stops it matching a strict prefix of the
// span its syntactic regex claimed. The classic engine localises a semantic failure by re-walking the
// syntactic token list, and a semantic match that ends mid-token makes it compute a highlight range past
// the end of the value (IntelliJ then logs "rangeInElement … must not exceed …").
private const val FIELD_END = """(?![^\s:])"""

private fun retainedField(head: String) = RegexTerminal(RETAINED_FIELD_SYNTAX, head + RETAINED_FIELD_BODY + FIELD_END)

/** An absolute path field (after unit_path_printf), as path_simplify_and_warn(PATH_CHECK_ABSOLUTE) checks it. */
val RETAINED_ABSOLUTE_PATH_FIELD = retainedField(NO_DOT_DOT_COMPONENT + ABSOLUTE_HEAD)

/** An absolute path field optionally prefixed by `-` ("ignore if missing"), as BindPaths= sources are. */
val RETAINED_OPTIONAL_ABSOLUTE_PATH_FIELD = retainedField(NO_DOT_DOT_COMPONENT + """(?=["']*-?["']*[/%])""")

/** MountImages= source: `-`-prefixable absolute path, not below /proc or /sys (PATH_CHECK_NON_API_VFS_DEV_OK). */
val MOUNT_IMAGE_SOURCE_FIELD = retainedField(NOT_PROC_OR_SYS + NO_DOT_DOT_COMPONENT + """(?=["']*-?["']*[/%])""")

/** MountImages= destination: absolute path, not below /proc or /sys (PATH_CHECK_NON_API_VFS_DEV_OK). */
val MOUNT_IMAGE_DESTINATION_FIELD = retainedField(NOT_PROC_OR_SYS + NO_DOT_DOT_COMPONENT + ABSOLUTE_HEAD)

/** ExtensionImages= source: `-`-prefixable absolute path, not below /proc, /sys or /dev (PATH_CHECK_NON_API_VFS). */
val EXTENSION_IMAGE_SOURCE_FIELD = retainedField(NOT_API_VFS + NO_DOT_DOT_COMPONENT + """(?=["']*-?["']*[/%])""")

/**
 * A free-form field — mount options, which systemd only runs through unit_full_printf() and hands to
 * mount(8) later. Only the escape rules are checked.
 */
val RETAINED_FREE_FIELD = retainedField("")

/**
 * The ':' run between fields of a MountImages=/ExtensionImages=/RootImageOptions= word. Those three
 * split WITHOUT EXTRACT_DONT_COALESCE_SEPARATORS, so `a::b` is the same as `a:b` and leading/trailing
 * colons are skipped.
 */
val COALESCED_COLONS = RegexTerminal(":+", ":+")

/**
 * A partition designator — partition_designator_from_string (src/shared/gpt.c#L156-L183). The table
 * names, plus the "-vty"/"-vsig" shorthands that partition_name_expand() rewrites to "-verity" /
 * "-verity-sig" (the "2" = "-secondary" shorthand expands to names the table does not contain, so it
 * is never accepted here). Matching is case-sensitive (string_table_lookup_from_string uses streq).
 */
fun partitionDesignator() = FlexibleLiteralChoiceTerminal(
  "root", "usr", "home", "srv", "esp", "xbootldr", "swap",
  "root-verity", "usr-verity", "root-verity-sig", "usr-verity-sig", "tmp", "var",
  "root-vty", "usr-vty", "root-vsig", "usr-vsig",
)

/**
 * The trailing `[:OPTIONS | (:PARTITION:OPTIONS)*[:OPTIONS]]` part of a MountImages=/ExtensionImages=
 * word: the loop in config_parse_mount_images / config_parse_extension_images reads pairs with
 * extract_many_words(partition, options); a lone trailing word is the root partition's options, while
 * a pair whose first word is not a partition designator is logged and skipped.
 */
fun imageMountOptionsTail(): Combinator = SequenceCombinator(
  ZeroOrMore(SequenceCombinator(COALESCED_COLONS, partitionDesignator(), COALESCED_COLONS, RETAINED_FREE_FIELD)),
  optionalNoReprobe(SequenceCombinator(COALESCED_COLONS, RETAINED_FREE_FIELD)),
  ZeroOrOne(COALESCED_COLONS),
)

private val EMPTY = RegexTerminal("", "")

/**
 * Like [ZeroOrOne], but without its second probe. ZeroOrOne tries its inner combinator again after a
 * match to report how far input "could" reach; when the inner can match twice in a row (`:OPTIONS` can),
 * a semantic failure further left then reports a longest-match at the very end of the value, and the
 * classic engine's error localisation indexes one past its token list (IndexOutOfBoundsException,
 * logged as an internal error). An alternative with an empty branch has no such probe.
 */
fun optionalNoReprobe(inner: Combinator): Combinator = AlternativeCombinator(inner, EMPTY)

/** A whitespace-separated list of [item]s ending the value. */
fun whitespaceList(item: Combinator): Combinator = SequenceCombinator(
  item,
  ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), item)),
  EOF(),
)

// ---------------------------------------------------------------------------------------------------
// Base64 — unbase64mem_full / unbase64_next / unbase64char (src/basic/hexdecoct.c#L528-L850).
//
// Whitespace is skipped anywhere. Both the standard (+/) and URL-safe (-_) alphabets are accepted. The
// non-blank characters must form complete 4-character groups; padding may only appear as the last one
// or two characters of the last group, and then the bits it discards must be zero (b & 15 / c & 3).

private const val B64 = """[A-Za-z0-9+/_-]"""
private const val B64_GROUPS = """(?:(?:\s*$B64){4})*"""
private const val B64_TAIL =
  """(?:\s*$B64\s*[AQgw]\s*=\s*=|\s*$B64\s*$B64\s*[AEIMQUYcgkosw048]\s*=)?\s*"""

/** Base64 as unbase64mem() accepts it, possibly empty. */
val BASE64_DATA = RegexTerminal("""[\s\S]*""", """$B64_GROUPS$B64_TAIL(?![\s\S])""")

/** Base64 as unbase64mem() accepts it, at least one group. */
val NON_EMPTY_BASE64_DATA = RegexTerminal("""[\s\S]+""", """(?=\s*\S)$B64_GROUPS$B64_TAIL(?![\s\S])""")

// ---------------------------------------------------------------------------------------------------
// Unsigned numbers as safe_atou() reads them: strtoul(…, base 0) plus mangle_base()'s Python-style
// "0b"/"0o" prefixes — decimal, 0x hex, leading-zero octal, 0b binary, 0o octal — and an optional '+'
// (strtoul accepts a sign; only a non-zero negative is refused). mangle_base() looks for 0b/0o BEFORE
// strtoul has consumed a sign, so "+0b1" is refused (strtoul reads "+0" and stops at 'b').

private val PLUS_SIGN = LiteralChoiceTerminal("+")

// "-0", "-00", "-0x0": strtoul accepts the sign and the result is 0, which `l != 0 && s[0] == '-'` lets
// through. ("-0b0"/"-0o0" are refused: mangle_base does not see past the '-'.)
private val NEGATIVE_ZERO = RegexTerminal("""-[0-9a-zA-Z]+""", """-(?:0[xX]0+|0+)(?![0-9a-zA-Z])""")

/**
 * An unsigned number in `[0, maxExclusive)` spelled any way safe_atou() accepts. The 0b/0o forms are
 * tried first (UnsignedNumberTerminal would otherwise read their leading 0 and strand the rest); their
 * upper bound is only enforced by [binaryOrOctalSemantic], which the caller supplies. With
 * [allowNegativeZero], a negative zero (`-0`, `-0x0`) is accepted too — only for callers where a
 * leading '-' cannot mean anything else (not NUMAMask=, where '-' is the range separator).
 */
fun safeAtouNumber(maxExclusive: Long, binaryOrOctalSemantic: String, allowNegativeZero: Boolean = false): Combinator {
  val alternatives = mutableListOf<Combinator>(
    RegexTerminal("""0[bB][0-9a-zA-Z]+|0[oO][0-9a-zA-Z]+""", """(?:$binaryOrOctalSemantic)(?![0-9a-zA-Z])"""),
  )
  if (allowNegativeZero) alternatives += NEGATIVE_ZERO
  alternatives += SequenceCombinator(ZeroOrOne(PLUS_SIGN), UnsignedNumberTerminal(0, maxExclusive))
  return AlternativeCombinator(*alternatives.toTypedArray())
}
