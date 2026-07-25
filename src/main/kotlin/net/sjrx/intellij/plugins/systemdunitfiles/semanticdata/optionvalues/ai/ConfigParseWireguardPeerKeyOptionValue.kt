package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/**
 * Validator for `[WireGuardPeer] PublicKey=` and `PresharedKey=` (.netdev).
 *
 * C function: config_parse_wireguard_peer_key in src/network/netdev/wireguard.c →
 * wireguard_decode_key_and_warn, which either reads a credential when the value starts with `@`, or
 * base64-decodes the value and requires exactly WG_KEY_LEN (32) bytes.
 *
 * 32 bytes is 43 base64 characters plus one `=` of padding; systemd's unbase64mem tolerates the
 * padding being omitted, so both spellings are accepted here.
 */
class ConfigParseWireguardPeerKeyOptionValue : SimpleGrammarOptionValues(
    "config_parse_wireguard_peer_key",
    SequenceCombinator(
        AlternativeCombinator(
            // @credential-name — resolved at load time via read_credential().
            SequenceCombinator(
                LiteralChoiceTerminal("@"),
                RegexTerminal("""\S+""", """[A-Za-z0-9_.\-]+""")
            ),
            // A base64-encoded 32-byte key.
            RegexTerminal("""[A-Za-z0-9+/=]+""", """[A-Za-z0-9+/]{43}=?"""),
        ),
        EOF()
    )
)
