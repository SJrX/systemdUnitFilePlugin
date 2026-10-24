package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Checked
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IPV6_ADDR
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ipIsNull
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ipLiteralBytes

/*
 * [Network] IPv6StableSecretAddress= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#IPv6StableSecretAddress=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L2036-L2082   config_parse_in_addr_non_null (AF_INET6)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/in-addr-util.c#L517-L530   in_addr_from_string
 * linux  https://man7.org/linux/man-pages/man3/inet_pton.3.html
 */

/**
 * Validator for config_parse_in_addr_non_null with ltype AF_INET6 — `[Network]
 * IPv6StableSecretAddress=` (.network).
 *
 * The value goes through in_addr_from_string(AF_INET6), i.e. inet_pton(AF_INET6): an IPv6 literal, no
 * prefix length and no `%zone`. An IPv4 literal is not accepted. The parser then refuses the
 * all-zero address (`::`, `0:0:0:0:0:0:0:0`, …) with "cannot be the ANY address", which is flagged
 * here as well.
 */
class ConfigParseInAddrNonNullIpv6OptionValue : SimpleGrammarOptionValues(
    "config_parse_in_addr_non_null",
    SequenceCombinator(
        Checked(IPV6_ADDR) { text -> ipLiteralBytes(text)?.let { !ipIsNull(it) } ?: false },
        EOF()
    )
)
