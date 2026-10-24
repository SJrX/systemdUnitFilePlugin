package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON_DELIMITED_INTERFACE_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [Network] VirtualEthernetExtra= (.nspawn).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#VirtualEthernetExtra=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd-nspawn.html#--network-veth-extra=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-settings.c#L455-L479   config_parse_veth_extra (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-network.c#L947-L975   veth_extra_parse
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188   extract_first_word (":", EXTRACT_DONT_COALESCE_SEPARATORS)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/socket-util.c#L785-L830   ifname_valid_full (flags 0)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-13-NSPAWN.nspawn.sh#L551-L552
 * linux  https://man7.org/linux/man-pages/man7/netdevice.7.html
 */

/**
 * The container-side name. veth_extra_parse() does not reject an invalid one: if it is empty or fails
 * ifname_valid() it is silently replaced by the host-side name. So anything without a further colon is
 * accepted here.
 */
private val CONTAINER_SIDE = RegexTerminal("""[^:]+""", """[^:]+""")

/**
 * Validator for the .nspawn `[Network] VirtualEthernetExtra=` setting (config_parse_veth_extra, ltype 0).
 *
 * veth_extra_parse() takes `HOST[:CONTAINER]`, split with extract_first_word(":",
 * EXTRACT_DONT_COALESCE_SEPARATORS). HOST must pass ifname_valid() (printable ASCII except `:`, `/`, `%`;
 * at most 15 bytes; not all digits, `.`, `..`, `all` or `default`). CONTAINER may be omitted or empty.
 * Anything after a second colon — even an empty field, as in `a:b:` — is an error. Errors are logged
 * and the setting ignored.
 *
 * Deliberately lenient: an invalid CONTAINER name (too long, containing `/`, …) is accepted, because
 * systemd silently falls back to the host-side name rather than rejecting it.
 */
class ConfigParseVethExtraOptionValue : SimpleGrammarOptionValues(
    "config_parse_veth_extra",
    SequenceCombinator(
        COLON_DELIMITED_INTERFACE_NAME,
        ZeroOrOne(SequenceCombinator(COLON, ZeroOrOne(CONTAINER_SIDE))),
        EOF(),
    )
)
