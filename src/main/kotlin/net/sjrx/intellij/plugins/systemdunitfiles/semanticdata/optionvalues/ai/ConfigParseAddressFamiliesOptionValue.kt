package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for RestrictAddressFamilies=.
 *
 * C function: config_parse_address_families in src/core/load-fragment.c → parse_address_families
 * in src/shared/parse-helpers.c:90. Accepts:
 *   - "none" (clears the set, sets allowlist)
 *   - optional leading "~" (invert / denylist mode), followed by a whitespace-separated list
 *     of address family names resolved by af_from_name
 *
 * The name set is the list of AF_* macros systemd's af_from_name knows about, which is
 * generated (src/basic/generate-af-list.sh) from the AF_* #defines in <sys/socket.h>, minus
 * AF_UNSPEC and AF_MAX. It can be reproduced by preprocessing <sys/socket.h> with
 * `cpp -dM`, keeping the `#define AF_*` lines (dropping AF_UNSPEC/AF_MAX), and taking the macro
 * names.
 *
 * Enumerating the names exactly (rather than the old loose RegexTerminal("AF_[A-Z0-9_]+"))
 * makes validation correct (AF_BOGUS is now rejected) and sets up grammar-based completion.
 *
 * Note: some of these names still resolve via af_from_name (the macro exists in libc headers)
 * even though the kernel removed the protocol — AF_DECnet (Linux 6.1), AF_IRDA (4.17),
 * AF_ECONET (3.5), AF_WANPIPE (2.6.21). These are valid-but-removed and are intended targets
 * of a future deprecation annotator (see GitHub #467 / address_families(7)).
 */
class ConfigParseAddressFamiliesOptionValue : SimpleGrammarOptionValues(
    "config_parse_address_families",
    SequenceCombinator(
        AlternativeCombinator(
            LiteralChoiceTerminal("none"),
            SequenceCombinator(
                ZeroOrOne(LiteralChoiceTerminal("~")),
                ADDRESS_FAMILY,
                ZeroOrMore(SequenceCombinator(
                    WhitespaceTerminal(),
                    ADDRESS_FAMILY
                ))
            )
        ),
        EOF()
    )
) {
    companion object {
        /**
         * The AF_* names systemd's af_from_name accepts. FlexibleLiteralChoiceTerminal matches
         * loosely for syntax (so coloring / error localization still work) but requires an exact
         * choice to be semantically valid.
         */
        private val ADDRESS_FAMILY = FlexibleLiteralChoiceTerminal(
            "AF_ALG",
            "AF_APPLETALK",
            "AF_ASH",
            "AF_ATMPVC",
            "AF_ATMSVC",
            "AF_AX25",
            "AF_BLUETOOTH",
            "AF_BRIDGE",
            "AF_CAIF",
            "AF_CAN",
            "AF_DECnet",
            "AF_ECONET",
            "AF_FILE",
            "AF_IB",
            "AF_IEEE802154",
            "AF_INET",
            "AF_INET6",
            "AF_IPX",
            "AF_IRDA",
            "AF_ISDN",
            "AF_IUCV",
            "AF_KCM",
            "AF_KEY",
            "AF_LLC",
            "AF_LOCAL",
            "AF_MCTP",
            "AF_MPLS",
            "AF_NETBEUI",
            "AF_NETLINK",
            "AF_NETROM",
            "AF_NFC",
            "AF_PACKET",
            "AF_PHONET",
            "AF_PPPOX",
            "AF_QIPCRTR",
            "AF_RDS",
            "AF_ROSE",
            "AF_ROUTE",
            "AF_RXRPC",
            "AF_SECURITY",
            "AF_SMC",
            "AF_SNA",
            "AF_TIPC",
            "AF_UNIX",
            "AF_VSOCK",
            "AF_WANPIPE",
            "AF_X25",
            "AF_XDP"
        )
    }
}
