package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import com.intellij.codeInspection.ProblemsHolder
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ALTERNATIVE_INTERFACE_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Combinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV4_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV6_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BatchUnitCoreGrammar.UNIT_ABSOLUTE_NON_API_VFS_PATH
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BatchUnitCoreGrammar.UNIT_ABSOLUTE_PATH
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BatchUnitCoreGrammar.UNIT_SPECIFIER_LETTERS
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BatchUnitCoreGrammar.UNIT_TEXT_CHAR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BatchUnitCoreGrammar.withUnitSpecifiers

/*
 * [Socket] ListenStream= / ListenDatagram= / ListenSequentialPacket= / ListenNetlink= (SOCKET_SOCKET),
 * ListenFIFO= (SOCKET_FIFO), ListenMessageQueue= (SOCKET_MQUEUE), ListenUSBFunction= (SOCKET_USB_FUNCTION)
 * and ListenSpecial= (SOCKET_SPECIAL).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.socket.html#ListenStream=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.socket.html#ListenFIFO=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.socket.html#ListenNetlink=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L603-L722   config_parse_socket_listen (SOCKET_SOCKET, SOCKET_FIFO, SOCKET_SPECIAL, SOCKET_MQUEUE, SOCKET_USB_FUNCTION)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/unit-printf.c#L253   unit_path_printf (specifiers expanded first, every ltype)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/specifier.c#L33-L105   specifier_printf (unknown %X is an error, %% is a literal %)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L34   path_simplify_and_warn (file ltypes: absolute, no "..", not below /dev /sys /proc unless SPECIAL)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/mountpoint-util.c#L721   path_below_api_vfs
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L574-L601   patch_var_run (/var/run/ -> /run/ before the length check)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/socket-netlink.c#L24-L96   socket_address_parse
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L1668   socket_address_parse_unix
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L1352   sockaddr_un_set_path (2..107 bytes)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L1754   socket_address_parse_vsock
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L1708   vsock_parse_port
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L1731   vsock_parse_cid
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L718   parse_ip_port (1..65535, safe_atou16_full with SAFE_ATO_REFUSE_LEADING_WHITESPACE)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478   safe_atou_full (vsock CID/port, netlink group)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/socket-netlink.c#L220   in_addr_port_ifindex_name_from_string_auto
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L785   ifname_valid_full (IFNAME_VALID_ALTERNATIVE | IFNAME_VALID_NUMERIC)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/socket-netlink.c#L119-L152   socket_address_parse_netlink
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L656   netlink_family_table (WITH_FALLBACK: a number up to INT_MAX also works)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/string-table.c#L65-L80   string_table_lookup_from_string_fallback (safe_atou, then <= max)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-socket-netlink.c#L40   TEST(socket_address_parse)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-socket-netlink.c#L105   TEST(socket_address_parse_netlink)
 * linux  https://man7.org/linux/man-pages/man7/unix.7.html
 * linux  https://man7.org/linux/man-pages/man7/netlink.7.html
 * linux  https://man7.org/linux/man-pages/man7/vsock.7.html
 * linux  https://man7.org/linux/man-pages/man7/mq_overview.7.html
 */

// ---------------------------------------------------------------------------------------------------
// Socket addresses (socket_address_parse), applied to the specifier-expanded text.

private val PERCENT_START_SPECIFIER = """%[$UNIT_SPECIFIER_LETTERS]"""

// AF_UNIX: '/' (file system) or '@' (abstract namespace), then anything. sockaddr_un_set_path insists on
// 2..107 bytes including the leading character. The length is only checked when no specifier is present,
// because a specifier's expansion length is unknown; a specifier may also stand at the start (%t/foo).
// config_parse_socket_listen runs patch_var_run() before parsing, which rewrites a /var/run/ prefix
// (path_startswith, so repeated slashes count as one) to /run/; the length limit applies to the result,
// so after /var/run/ up to 102 characters may follow. Length is counted in UTF-16 units, not bytes, so
// non-ASCII paths are checked leniently.
private val UNIX_SOCKET_ADDRESS = RegexTerminal(
  """[/@%][\s\S]*""",
  """(?:(?=[^%]*$)(?:/+var/+run/+[\s\S]{0,102}|[/@][\s\S]{1,106})|(?=[\s\S]*%)(?:[/@]|$PERCENT_START_SPECIFIER)$UNIT_TEXT_CHAR*)$"""
)

// parse_ip_port: safe_atou16_full(…, SAFE_ATO_REFUSE_LEADING_WHITESPACE), then 0 is refused. The port is a
// whole value or the text after the last ':' (cut before a '%' scope), so a leading `\v` and `0b 1` reach
// the parser (accepted) while a leading space is refused (sd-check: `ListenStream=1.2.3.4: 80`).
private val IP_PORT = SafeAtoTerminal(
  signed = false,
  base = 0,
  flags = setOf(SafeAtoTerminal.Flag.REFUSE_LEADING_WHITESPACE),
  minInclusive = 1L,
  maxInclusive = 65_535L,
  acceptsWhitespace = true,
  acceptsMinus = true,
)

// AF_VSOCK: vsock[-stream|-dgram|-seqpacket]:CID:PORT; the CID may be empty (VMADDR_CID_ANY), one of the
// named CIDs, "-1", or a number; the port is any unsigned except VMADDR_PORT_ANY (UINT32_MAX). Both numbers
// are plain safe_atou() on a field cut at the first ':', so whitespace reaches the parser
// (sd-check accepts `vsock:2: 80` and `vsock: 2:80`).
private val VSOCK_CID = AlternativeCombinator(
  FlexibleLiteralChoiceTerminal("hypervisor", "local", "host", "any"),
  LiteralChoiceTerminal("-1"),
  SafeAtoTerminal(
    signed = false,
    base = 0,
    flags = emptySet(),
    minInclusive = 0L,
    maxInclusive = 4_294_967_295L,
    acceptsWhitespace = true,
    acceptsMinus = true,
  ),
)
private val VSOCK_ADDRESS = SequenceCombinator(
  LiteralChoiceTerminal("vsock-seqpacket:", "vsock-stream:", "vsock-dgram:", "vsock:"),
  ZeroOrOne(VSOCK_CID),
  COLON,
  SafeAtoTerminal(
    signed = false,
    base = 0,
    flags = emptySet(),
    minInclusive = 0L,
    maxInclusive = 4_294_967_294L,
    acceptsWhitespace = true,
    acceptsMinus = true,
  ),
)

// An interface scope after the port. The `%` that introduces it has to be written `%%` in a unit file:
// unit_path_printf() runs first, and `%eth0` is an unknown specifier (an error), `%lo` would expand %l.
private val INTERFACE_SCOPE = SequenceCombinator(
  LiteralChoiceTerminal("%%"),
  AlternativeCombinator(ALTERNATIVE_INTERFACE_NAME, RegexTerminal("[1-9][0-9]*", "[1-9][0-9]*")),
)

private val IPV6_SOCKET_ADDRESS = SequenceCombinator(
  LiteralChoiceTerminal("["), IPV6_ADDR, LiteralChoiceTerminal("]"), COLON, IP_PORT, ZeroOrOne(INTERFACE_SCOPE),
)
private val IPV4_SOCKET_ADDRESS = SequenceCombinator(IPV4_ADDR, COLON, IP_PORT, ZeroOrOne(INTERFACE_SCOPE))

/** Anything socket_address_parse() accepts — the grammar for ListenStream= and ListenDatagram=. */
private val SOCKET_ADDRESS = withUnitSpecifiers(
  AlternativeCombinator(
    SequenceCombinator(UNIX_SOCKET_ADDRESS, EOF()),
    SequenceCombinator(VSOCK_ADDRESS, EOF()),
    SequenceCombinator(IPV6_SOCKET_ADDRESS, EOF()),
    SequenceCombinator(IPV4_SOCKET_ADDRESS, EOF()),
    SequenceCombinator(IP_PORT, EOF()),
  )
)

/** ListenSequentialPacket=: config_parse_socket_listen refuses every family but AF_UNIX for SOCK_SEQPACKET. */
private val SEQPACKET_ADDRESS = withUnitSpecifiers(SequenceCombinator(UNIX_SOCKET_ADDRESS, EOF()))

/**
 * ListenNetlink=: `FAMILY [GROUP]`, the family a name from netlink_family_table or a number.
 *
 * socket_address_parse_netlink() takes the family as the first word (extract_first_word, split on
 * `" \t\n\r"` only, so `route\v5` is one word and refused) and hands the whole REST to safe_atou(), so
 * the group may contain whitespace that strtoul skips (sd-check accepts `route 0b 1` and `route \v5`).
 */
private val NETLINK_ADDRESS = withUnitSpecifiers(
  SequenceCombinator(
    // The number goes first: the name list contains digits and '-' (ip6-fw), so its syntactic shape also
    // matches the `0b` of `0b+1`, and the classic engine commits to the first syntactic match.
    AlternativeCombinator(
      SafeAtoTerminal(
        signed = false,
        base = 0,
        flags = emptySet(),
        minInclusive = 0L,
        maxInclusive = Int.MAX_VALUE.toLong(),
        acceptsWhitespace = false,
        acceptsMinus = true,
      ),
      FlexibleLiteralChoiceTerminal(
        "route", "firewall", "inet-diag", "nflog", "xfrm", "selinux", "iscsi", "audit", "fib-lookup",
        "connector", "netfilter", "ip6-fw", "dnrtmsg", "kobject-uevent", "generic", "scsitransport",
        "ecryptfs", "rdma",
      ),
    ),
    ZeroOrOne(
      SequenceCombinator(
        RegexTerminal("[ \t\n\r]+", "[ \t\n\r]+"),
        SafeAtoTerminal(
          signed = false,
          base = 0,
          flags = emptySet(),
          minInclusive = 0L,
          maxInclusive = 4_294_967_295L,
          acceptsWhitespace = true,
          acceptsMinus = true,
        ),
      )
    ),
    EOF(),
  )
)

/**
 * Validator for the four `[Socket]` keys config_parse_socket_listen handles with ltype SOCKET_SOCKET.
 *
 * The ltype is shared but the value type is not — config_parse_socket_listen branches on the key name
 * itself — so this dispatches on [UnitFilePropertyType.key]:
 *
 *  - `ListenStream=` / `ListenDatagram=`: any socket_address_parse() address — an absolute path or
 *    `@abstract` name (2..107 bytes), `vsock:CID:PORT` (also `vsock-stream:` etc.), a bare port
 *    (1..65535; parse_ip_port, so `0x50` is port 80), `IPv4:PORT` or `[IPv6]:PORT`, the latter two
 *    optionally followed by an interface scope. Because unit_path_printf() runs first, that scope's
 *    `%` must be written `%%` (`[fe80::1]:80%%eth0`).
 *  - `ListenSequentialPacket=`: AF_UNIX only.
 *  - `ListenNetlink=`: a netlink family name or number, optionally followed by a multicast group.
 *
 * Any value that uses a known unit specifier is accepted. [combinator] (used for colouring, completion
 * and anything key-agnostic) is the union of the per-key grammars.
 */
class ConfigParseSocketListenSocketOptionValue : SimpleGrammarOptionValues(
  "config_parse_socket_listen",
  AlternativeCombinator(SOCKET_ADDRESS, NETLINK_ADDRESS)
) {
  private val byKey: Map<String, SimpleGrammarOptionValues> = mapOf(
    "ListenStream" to SimpleGrammarOptionValues("config_parse_socket_listen", SOCKET_ADDRESS),
    "ListenDatagram" to SimpleGrammarOptionValues("config_parse_socket_listen", SOCKET_ADDRESS),
    "ListenSequentialPacket" to SimpleGrammarOptionValues("config_parse_socket_listen", SEQPACKET_ADDRESS),
    "ListenNetlink" to SimpleGrammarOptionValues("config_parse_socket_listen", NETLINK_ADDRESS),
  )

  /** The grammar config_parse_socket_listen applies to [key]; the union for an unexpected key. */
  fun combinatorForKey(key: String): Combinator = byKey[key]?.combinator ?: combinator

  override fun generateProblemDescriptors(property: UnitFilePropertyType, holder: ProblemsHolder) {
    val delegate = byKey[property.key]
    if (delegate != null) delegate.generateProblemDescriptors(property, holder) else super.generateProblemDescriptors(property, holder)
  }
}

/**
 * Validator for `[Socket] ListenFIFO=`, `ListenMessageQueue=` and `ListenUSBFunction=` (ltypes
 * SOCKET_FIFO, SOCKET_MQUEUE, SOCKET_USB_FUNCTION).
 *
 * After unit_path_printf() the value goes through path_simplify_and_warn(PATH_CHECK_ABSOLUTE |
 * PATH_CHECK_NON_API_VFS): it must be absolute (or start with a specifier such as `%t`), contain no
 * `..` component, and not be /dev, /sys, /proc or below. A message queue name is a single-component
 * path like `/queue`; systemd does not enforce that at parse time, so neither does this.
 */
class ConfigParseSocketListenFileOptionValue : SimpleGrammarOptionValues(
  "config_parse_socket_listen",
  SequenceCombinator(UNIT_ABSOLUTE_NON_API_VFS_PATH, EOF())
)

/**
 * Validator for `[Socket] ListenSpecial=` (ltype SOCKET_SPECIAL): as
 * [ConfigParseSocketListenFileOptionValue] but without PATH_CHECK_NON_API_VFS, because special files
 * live precisely in /dev, /proc and /sys (`ListenSpecial=/dev/rfkill`).
 */
class ConfigParseSocketListenSpecialOptionValue : SimpleGrammarOptionValues(
  "config_parse_socket_listen",
  SequenceCombinator(UNIT_ABSOLUTE_PATH, EOF())
)
