package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.CREDENTIAL_REFERENCE
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WIREGUARD_BASE64_KEY

/*
 * [WireGuard] PrivateKey= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#PrivateKey=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/wireguard.c#L546-L561   config_parse_wireguard_private_key (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/wireguard.c#L488-L544   wireguard_decode_key_and_warn
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/hexdecoct.c#L755-L851   unbase64mem_full
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/hexdecoct.c#L528-L555   unbase64char (also accepts the URL-safe '-' and '_')
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/creds-util.c#L156-L180   read_credential (credential_name_valid)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-wireguard.netdev#L12
 */

/**
 * Validator for `[WireGuard] PrivateKey=` (.netdev).
 *
 * C function: config_parse_wireguard_private_key → wireguard_decode_key_and_warn. Either `@name`,
 * which reads the key from the named credential (the name must pass credential_name_valid), or the key
 * itself in base64, which must decode to exactly 32 bytes: 43 characters plus one mandatory `=` of
 * padding, the 43rd character restricted to those whose low two bits are clear. See
 * [WIREGUARD_BASE64_KEY]. An invalid value is logged and ignored by systemd.
 */
class ConfigParseWireguardPrivateKeyOptionValue : SimpleGrammarOptionValues(
    "config_parse_wireguard_private_key",
    SequenceCombinator(AlternativeCombinator(CREDENTIAL_REFERENCE, WIREGUARD_BASE64_KEY), EOF())
)
