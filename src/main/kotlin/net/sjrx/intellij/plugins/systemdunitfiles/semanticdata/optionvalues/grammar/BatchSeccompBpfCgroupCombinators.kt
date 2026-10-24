package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

/*
 * Shared terminals for the seccomp / BPF / cgroup validators (batch 2026-10).
 *
 * Everything here is pinned to systemd 0f0682b773, the commit in systemd-build/build/last_commit_hash
 * that the plugin's gperf and man data are generated from. Browse it at
 * https://github.com/systemd/systemd/blob/0f0682b773.
 *
 * These live in their own file rather than Combinators.kt because several validator families were
 * written in parallel; the generally useful ones (ERRNO_OR_SECCOMP_ACTION, PRINTF_CHAR, the
 * whitespace-or-comma separator) are candidates for promotion.
 */

/**
 * One run of separators for a list split with `extract_first_word(&p, &word, WHITESPACE ",", …)`.
 *
 * extract_first_word skips any leading separators and coalesces runs of them (unless
 * EXTRACT_DONT_COALESCE_SEPARATORS is passed, which none of these callers do), so `a, b`, `a,,b` and
 * `a b` all split into the same two words.
 *
 * https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10
 */
val WHITESPACE_OR_COMMA_SEPARATOR = RegexTerminal("[\\s,]+", "[\\s,]+")

// ---------------------------------------------------------------------------------------------------
// errno names / numbers — parse_errno (src/basic/parse-util.c#L342) and
// seccomp_parse_errno_or_action (src/shared/seccomp-util.c#L2688).

/**
 * The errno names errno_from_name() knows: the `#define E…` macros of `<errno.h>`, as listed by
 * src/basic/generate-errno-list.sh (which drops the parisc-only aliases ECANCELLED, EREFUSED,
 * EFSBADCRC and EFSCORRUPTED). Regenerate with
 *
 *     echo | gcc -dM -include errno.h -E - | grep -Ev '^#define[[:space:]]+(ECANCELLED|EREFUSED|EFSBADCRC|EFSCORRUPTED)' \
 *       | awk '/^#define[ \t]+E[^ _]+[ \t]+/ { print $2; }' | sort
 *
 * The lookup table is built by gperf with `--ignore-case` (src/basic/meson.build#L168), so `eperm`
 * resolves as well as `EPERM`.
 */
val ERRNO_NAMES = arrayOf(
  "E2BIG", "EACCES", "EADDRINUSE", "EADDRNOTAVAIL", "EADV", "EAFNOSUPPORT", "EAGAIN", "EALREADY", "EBADE", "EBADF",
  "EBADFD", "EBADMSG", "EBADR", "EBADRQC", "EBADSLT", "EBFONT", "EBUSY", "ECANCELED", "ECHILD", "ECHRNG",
  "ECOMM", "ECONNABORTED", "ECONNREFUSED", "ECONNRESET", "EDEADLK", "EDEADLOCK", "EDESTADDRREQ", "EDOM", "EDOTDOT", "EDQUOT",
  "EEXIST", "EFAULT", "EFBIG", "EHOSTDOWN", "EHOSTUNREACH", "EHWPOISON", "EIDRM", "EILSEQ", "EINPROGRESS", "EINTR",
  "EINVAL", "EIO", "EISCONN", "EISDIR", "EISNAM", "EKEYEXPIRED", "EKEYREJECTED", "EKEYREVOKED", "EL2HLT", "EL2NSYNC",
  "EL3HLT", "EL3RST", "ELIBACC", "ELIBBAD", "ELIBEXEC", "ELIBMAX", "ELIBSCN", "ELNRNG", "ELOOP", "EMEDIUMTYPE",
  "EMFILE", "EMLINK", "EMSGSIZE", "EMULTIHOP", "ENAMETOOLONG", "ENAVAIL", "ENETDOWN", "ENETRESET", "ENETUNREACH", "ENFILE",
  "ENOANO", "ENOBUFS", "ENOCSI", "ENODATA", "ENODEV", "ENOENT", "ENOEXEC", "ENOKEY", "ENOLCK", "ENOLINK",
  "ENOMEDIUM", "ENOMEM", "ENOMSG", "ENONET", "ENOPKG", "ENOPROTOOPT", "ENOSPC", "ENOSR", "ENOSTR", "ENOSYS",
  "ENOTBLK", "ENOTCONN", "ENOTDIR", "ENOTEMPTY", "ENOTNAM", "ENOTRECOVERABLE", "ENOTSOCK", "ENOTSUP", "ENOTTY", "ENOTUNIQ",
  "ENXIO", "EOPNOTSUPP", "EOVERFLOW", "EOWNERDEAD", "EPERM", "EPFNOSUPPORT", "EPIPE", "EPROTO", "EPROTONOSUPPORT", "EPROTOTYPE",
  "ERANGE", "EREMCHG", "EREMOTE", "EREMOTEIO", "ERESTART", "ERFKILL", "EROFS", "ESHUTDOWN", "ESOCKTNOSUPPORT", "ESPIPE",
  "ESRCH", "ESRMNT", "ESTALE", "ESTRPIPE", "ETIME", "ETIMEDOUT", "ETOOMANYREFS", "ETXTBSY", "EUCLEAN", "EUNATCH",
  "EUSERS", "EWOULDBLOCK", "EXDEV", "EXFULL",
)

/** An errno name as errno_from_name() resolves it (case-insensitively). */
val ERRNO_NAME = FlexibleLiteralChoiceTerminal(*ERRNO_NAMES, ignoreCase = true)

/**
 * A signed integer written the way systemd's safe_atoi() reads one (src/basic/parse-util.c#L497),
 * range-checked in its real base.
 *
 * safe_atoi() first applies mangle_base() (#L403), which turns a leading `0b`/`0B` into base 2 and
 * `0o`/`0O` into base 8, and otherwise calls strtol() with base 0 — so an optional `+`/`-` sign, then
 * `0x` hex, leading-`0` octal or decimal. [UnsignedNumberTerminal] covers only the unsigned base-0
 * spellings, which would flag `+5`, `0b11` or `-0`, all of which systemd takes.
 */
class SafeAtoiTerminal(private val minInclusive: Long, private val maxExclusive: Long) : TerminalCombinator {

  private fun span(value: String, offset: Int): String? = SHAPE.matchAt(value, offset)?.value

  private fun valueOf(text: String): Long? {
    val lower = text.lowercase()
    if (lower.startsWith("0b")) return lower.drop(2).toLongOrNull(2)
    if (lower.startsWith("0o")) return lower.drop(2).toLongOrNull(8)
    val negative = lower.startsWith("-")
    val digits = lower.removePrefix("+").removePrefix("-")
    val magnitude = when {
      digits.startsWith("0x") -> digits.drop(2).toLongOrNull(16)
      digits.length > 1 && digits.startsWith("0") -> digits.drop(1).toLongOrNull(8)
      else -> digits.toLongOrNull()
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

  override fun toString(): String = "SafeAtoi($minInclusive,$maxExclusive)"

  private companion object {
    // Prefixed forms first, so "0x10" / "0b11" aren't read as a lone 0. The 0b/0o forms take no sign:
    // mangle_base only looks at the very first characters, and strtol() would stop at the 'b'/'o'.
    val SHAPE = Regex("""0[bB][01]+|0[oO][0-7]+|[+-]?(?:0[xX][0-9a-fA-F]+|0[0-7]+|[0-9]+)""")
  }
}

/**
 * The argument of a `syscall:ERRNO` entry (and of SystemCallErrorNumber=): `kill`, an errno name, or a
 * number. seccomp_parse_errno_or_action() special-cases `kill` (exact, lower case) and otherwise calls
 * parse_errno(), which takes a name first and then falls back to safe_atoi() ([SafeAtoiTerminal]: sign,
 * `0x`, `0b`, `0o`, leading-`0` octal), accepting 0 and 1…ERRNO_MAX (4095) — so `-0` is fine but `-3` is
 * not.
 *
 * The number comes before the name because ERRNO_NAME's lenient shape includes digits (E2BIG): under
 * the classic engine, with the name first, `0x1F` would be taken as the one-character name `0` and the
 * rest stranded.
 */
val ERRNO_OR_SECCOMP_ACTION = AlternativeCombinator(
  LiteralChoiceTerminal("kill"),
  SafeAtoiTerminal(0, 4096),
  ERRNO_NAME,
)

// ---------------------------------------------------------------------------------------------------
// System call names and groups — seccomp_parse_syscall_filter (src/shared/seccomp-util.c#L1293).

/**
 * The predefined system call groups, without their leading `@` (syscall_filter_sets[],
 * src/shared/seccomp-util.c#L334; looked up by exact string compare in syscall_filter_set_find,
 * #L1016). An unknown group is logged ("Unknown system call group, ignoring") and dropped.
 */
val SYSCALL_GROUP_NAME = FlexibleLiteralChoiceTerminal(
  "default", "aio", "basic-io", "chown", "clock", "cpu-emulation", "debug", "file-system", "io-event", "ipc",
  "keyring", "memlock", "module", "mount", "network-io", "obsolete", "pkey", "privileged", "process", "raw-io",
  "reboot", "resources", "sandbox", "setuid", "signal", "swap", "sync", "system-service", "timer", "known",
)

/**
 * A bare system call name. These are resolved at runtime by libseccomp's
 * seccomp_syscall_resolve_name() for the *native* architecture, so whether e.g. `arm_fadvise64_64` is
 * "known" depends on the machine; an unknown name is logged and ignored. The set is therefore not
 * enumerated here: the only check is the shape every libseccomp name has (lower case letters, digits
 * and underscores — `_llseek`, `epoll_wait`, `s390_pci_mmio_read`). The lenient syntactic side takes
 * anything up to a separator or `:`, so `DONOTEXIST` is located and flagged as a single token.
 */
val SYSCALL_NAME = RegexTerminal("[^\\s,:]+", "[a-z_][a-z0-9_]*")

/** `@group` or a bare system call name. */
val SYSCALL_OR_GROUP = AlternativeCombinator(
  SequenceCombinator(LiteralChoiceTerminal("@"), SYSCALL_GROUP_NAME),
  SYSCALL_NAME,
)

// ---------------------------------------------------------------------------------------------------
// File system names — bpf_restrict_fs_parse_filesystem (src/core/bpf-restrict-fs.c#L212).

/**
 * The file system names fs_type_from_string() resolves: the keys of NAME_TO_MAGIC in
 * src/basic/filesystem-sets.py, compiled into a case-sensitive gperf table. A name outside this set is
 * accepted by the unit-file parser but logged and skipped when the BPF map is filled
 * ("bpf-restrict-fs: Invalid filesystem name '%s', ignoring.", src/core/bpf-restrict-fs.c#L117-L121).
 */
val FILESYSTEM_NAME = FlexibleLiteralChoiceTerminal(
  "apparmorfs", "adfs", "affs", "afs", "anon_inodefs", "autofs", "balloon-kvm", "bcachefs", "bdev", "binder",
  "binfmt_misc", "bpf", "btrfs", "btrfs_test_fs", "cpuset", "ceph", "cgroup2", "cgroup", "cifs", "coda",
  "configfs", "cramfs", "dax", "debugfs", "devmem", "devpts", "devtmpfs", "dmabuf", "ecryptfs", "efivarfs",
  "efs", "erofs", "ext2", "ext3", "ext4", "exfat", "f2fs", "failfs", "fuseblk", "fuse",
  "fusectl", "gfs", "gfs2", "gmem", "hostfs", "hpfs", "hugetlbfs", "iso9660", "jffs2", "minix",
  "mqueue", "msdos", "ncp", "ncpfs", "nfs", "nfs4", "nilfs2", "nsfs", "ntfs", "ntfs3",
  "nullfs", "ocfs2", "openpromfs", "orangefs", "overlay", "pidfs", "pipefs", "ppc-cmm", "proc", "pstore",
  "pvfs2", "qnx4", "qnx6", "ramfs", "resctrl", "reiserfs", "rpc_pipefs", "secretmem", "securityfs", "selinuxfs",
  "shiftfs", "smackfs", "smb3", "smbfs", "sockfs", "squashfs", "sysfs", "tmpfs", "tracefs", "udf",
  "usbdevfs", "vboxsf", "vfat", "v9fs", "xenfs", "xfs", "z3fold", "zonefs", "zsmalloc",
)

/**
 * The file system groups, without the leading `@` (FILESYSTEM_SETS in src/basic/filesystem-sets.py,
 * looked up by exact compare in filesystem_set_find, src/basic/filesystems.c#L21). An unknown group is
 * logged ("Unknown filesystem group, ignoring") and dropped.
 */
val FILESYSTEM_GROUP_NAME = FlexibleLiteralChoiceTerminal(
  "basic-api", "anonymous", "application", "auxiliary-api", "common-block", "historical-block", "network",
  "privileged-api", "security", "temporary", "known",
)

// ---------------------------------------------------------------------------------------------------
// Specifier expansion — unit_full_printf_full (src/core/unit-printf.c#L179).

/**
 * The specifier letters unit_full_printf() knows (src/core/unit-printf.c#L210-L238 plus
 * COMMON_SYSTEM/CREDS/TMP_SPECIFIERS from src/shared/specifier.h#L76-L99), and `%` for a literal
 * percent sign. Same set as ConfigParseUnitStringPrintfOptionValue uses.
 */
const val UNIT_SPECIFIER_LETTERS = "iIjJnNpPfyYcrRCdDELSthsaAbBHlqmMovwWgGuUTV%"

/**
 * A regex fragment for one character (or `%x` pair) of specifier-expanded text, per specifier_printf
 * (src/shared/specifier.c#L33): a known specifier expands; an unknown *alphanumeric* one is an error
 * (-EBADSLT, the assignment is dropped); `%` followed by anything else — or at the very end — is kept
 * literally. Use inside a character-class-free regex, e.g. `(?:$PRINTF_CHAR)*`.
 */
const val PRINTF_CHAR = "(?:[^%]|%[$UNIT_SPECIFIER_LETTERS]|%(?![A-Za-z0-9]))"

/** [PRINTF_CHAR], but also excluding [excluded] (a character-class body, e.g. `=` or `\\s`) as a plain character. */
fun printfCharExcluding(excluded: String): String = "(?:[^%$excluded]|%[$UNIT_SPECIFIER_LETTERS]|%(?![A-Za-z0-9]))"

// ---------------------------------------------------------------------------------------------------
// BPF token delegation lists — bpf_delegate_from_string (src/core/namespace.c#L4221).

/**
 * The value grammar shared by BPFDelegateCommands= / BPFDelegateMaps= / BPFDelegatePrograms= /
 * BPFDelegateAttachments=: either the whole value `any`, or a comma-separated list of names from
 * [names].
 *
 * bpf_delegate_from_string() checks `streq(s, "any")` against the whole value, then splits on `,`
 * *only* — no whitespace — with extract_first_word(..., ",", 0), which skips leading commas and
 * coalesces runs of them. So `A, B` makes a word ` B` that is not in the table ("Unknown BPF delegate
 * option, ignoring") and is flagged here, while `A,,B` and a trailing comma are fine. Each word is
 * looked up with DEFINE_STRING_TABLE_LOOKUP, i.e. an exact, case-sensitive streq.
 */
fun bpfDelegateList(names: FlexibleLiteralChoiceTerminal): Combinator {
  val commas = RegexTerminal(",+", ",+")
  return AlternativeCombinator(
    SequenceCombinator(LiteralChoiceTerminal("any"), EOF()),
    SequenceCombinator(
      ZeroOrOne(commas),
      ZeroOrOne(
        SequenceCombinator(
          names,
          ZeroOrMore(SequenceCombinator(commas, names)),
          ZeroOrOne(commas),
        )
      ),
      EOF(),
    ),
  )
}
