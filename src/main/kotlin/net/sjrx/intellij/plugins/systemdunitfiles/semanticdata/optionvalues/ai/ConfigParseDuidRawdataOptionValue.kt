package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NETWORK_DHCP_MISC_COALESCED_COLONS
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Repeat
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * DUIDRawData= — [DHCPv6] DUIDRawData= (.network, config_parse_duid_rawdata), [DHCPv4] DUIDRawData= and
 * the old [DHCP] DUIDRawData= (.network, config_parse_network_duid_rawdata), and [DHCPv4]/[DHCPv6]
 * DUIDRawData= in networkd.conf (config_parse_duid_rawdata).
 *
 * Note: the plugin reports the deprecated [DHCP] section as not allowed in .network files, so this
 * validator never actually runs for keys there; they are listed only because the gperf shares the parser.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/networkd.conf.html#DUIDRawData=
 *        https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#DUIDRawData=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L1321-L1392  config_parse_duid_rawdata (0)
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-dhcp-common.c#L1418-L1439  config_parse_network_duid_rawdata (0) → config_parse_duid_rawdata
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188              extract_first_word (":" separator, coalescing)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/libsystemd-network/dhcp-duid-internal.h#L26  MAX_DUID_DATA_LEN (128)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/network/test-networkd-conf.c#L108-L122
 *        https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-dhcp-client-ipv6-only-custom-client-identifier.network#L15
 */

// One byte: one or two hex digits (`0` is 0x00, `ab` is 0xab). The shape is wider so `000` / `abc`
// highlight as a single bad byte.
private val DUID_BYTE = RegexTerminal("""[0-9a-zA-Z]+""", """[0-9a-fA-F]{1,2}(?![0-9a-zA-Z])""")

/**
 * Validator for `DUIDRawData=`, registered for both config_parse_duid_rawdata and
 * config_parse_network_duid_rawdata.
 *
 * Colon-separated bytes `NN:NN:…`, each one or two hex digits, at most MAX_DUID_DATA_LEN = 128 of them.
 * The separator is extract_first_word(…, ":", 0), so runs of colons coalesce and leading/trailing colons
 * are accepted — test-networkd-conf.c accepts `00:11:22:` and `11::`, and rejects `000:11:22`,
 * `00:111:22` and `abcdef`. A value of nothing but colons parses to an empty DUID and is accepted too.
 */
class ConfigParseDuidRawdataOptionValue(validatorName: String = "config_parse_duid_rawdata") : SimpleGrammarOptionValues(
    validatorName,
    SequenceCombinator(
        ZeroOrMore(COLON),
        ZeroOrOne(
            SequenceCombinator(
                DUID_BYTE,
                Repeat(SequenceCombinator(NETWORK_DHCP_MISC_COALESCED_COLONS, DUID_BYTE), 0, 127),
                ZeroOrMore(COLON),
            ),
        ),
        EOF(),
    )
)
