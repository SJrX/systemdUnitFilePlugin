package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON_DELIMITED_INTERFACE_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Combinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [Network] Interface= / MACVLAN= / IPVLAN= (.nspawn).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#Interface=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#MACVLAN=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-settings.c#L481-L500   config_parse_network_iface_pair (0)
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-settings.c#L502-L521   config_parse_macvlan_iface_pair (0)
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-settings.c#L523-L542   config_parse_ipvlan_iface_pair (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-network.c#L999-L1045   network_iface_pair_parse
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188   extract_first_word
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L785-L830   ifname_valid_full (flags 0)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-13-NSPAWN.nspawn.sh#L553-L555
 * linux  https://man7.org/linux/man-pages/man7/netdevice.7.html
 */

/** `HOST[:CONTAINER]`, where an empty CONTAINER after the colon means "not given". */
private val INTERFACE_PAIR = SequenceCombinator(
    COLON_DELIMITED_INTERFACE_NAME,
    ZeroOrOne(SequenceCombinator(COLON, ZeroOrOne(COLON_DELIMITED_INTERFACE_NAME))),
)

private val INTERFACE_PAIR_LIST: Combinator = SequenceCombinator(
    INTERFACE_PAIR,
    ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), INTERFACE_PAIR)),
    EOF(),
)

/**
 * The shared grammar of the .nspawn `[Network] Interface=`, `MACVLAN=` and `IPVLAN=` settings, whose
 * three parsers are thin wrappers around network_iface_pair_parse() (they differ only in the `mv-` /
 * `iv-` prefix used when the container-side name is omitted).
 *
 * The value is a whitespace-separated list (extract_first_word with WHITESPACE). Each word is split once
 * at the first `:`: the host-side name must pass ifname_valid() (printable ASCII except `:`, `/`, `%`; at
 * most 15 bytes; not all digits, `.`, `..`, `all` or `default`, and nothing parse_ifindex() reads as a
 * positive index such as `0x10` or `+5`), and the container-side name, if present and non-empty, must
 * too — so `eth0:` is the same as `eth0`, and a second colon is an invalid name. Unlike most nspawn
 * settings an invalid word is a hard error that fails loading the file.
 *
 * Backslash escapes: each word is unescaped twice (whitespace split, then colon split), so a name that
 * runs into whitespace after a backslash run that is not a multiple of 4 long is refused, as C refuses it —
 * `eth0\ x` is one invalid name `eth0 x`, and `eth0\\ x` leaves a trailing backslash for the colon split.
 *
 * The prefixed default name (`mv-eth0`) may exceed 15 bytes; nspawn shortens it later, so the
 * host-side name alone is what is length-checked.
 */
open class ConfigParseInterfacePairOptionValue(validatorName: String) : SimpleGrammarOptionValues(validatorName, INTERFACE_PAIR_LIST)

/** `[Network] Interface=` — config_parse_network_iface_pair (0). */
class ConfigParseNetworkIfacePairOptionValue : ConfigParseInterfacePairOptionValue("config_parse_network_iface_pair")

/** `[Network] MACVLAN=` — config_parse_macvlan_iface_pair (0). */
class ConfigParseMacvlanIfacePairOptionValue : ConfigParseInterfacePairOptionValue("config_parse_macvlan_iface_pair")

/** `[Network] IPVLAN=` — config_parse_ipvlan_iface_pair (0). */
class ConfigParseIpvlanIfacePairOptionValue : ConfigParseInterfacePairOptionValue("config_parse_ipvlan_iface_pair")
