package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Repeat
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [MACsecTransmitAssociation] / [MACsecReceiveAssociation] KeyId= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#KeyId=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/macsec.c#L823-L879   config_parse_macsec_key_id (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/hexdecoct.c#L109-L158   unhexmem_full
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/include/uapi/linux/if_macsec.h#L23   MACSEC_KEYID_LEN (16)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-macsec.netdev#L12   KeyId=01 … KeyId=0203040506070809
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-hexdecoct.c#L110   TEST(unhexmem)
 */

// One byte: two hex digits. unhex_next() skips whitespace around every digit, so a space may even
// split a byte.
private val KEY_ID_BYTE = RegexTerminal("[0-9a-fA-F][ \\t]*[0-9a-fA-F]", "[0-9a-fA-F][ \\t]*[0-9a-fA-F]")

/**
 * Validator for `KeyId=` in `[MACsecTransmitAssociation]` and `[MACsecReceiveAssociation]` (.netdev).
 *
 * C function: config_parse_macsec_key_id, which decodes the value with unhexmem(): an even number of
 * hex digits (case-insensitive, whitespace between digits ignored) encoding at most MACSEC_KEYID_LEN =
 * 16 bytes, i.e. 2 to 32 digits. Shorter ids are zero-padded.
 *
 * The man page still says "Takes a number between 0-255", but the parser is hex-only: `KeyId=1` (odd
 * length) and `KeyId=255` are refused, while systemd's own fixtures write `KeyId=01` and
 * `KeyId=0203040506070809`. This follows the C. An invalid value is logged and ignored by systemd.
 */
class ConfigParseMacsecKeyIdOptionValue : SimpleGrammarOptionValues(
    "config_parse_macsec_key_id",
    SequenceCombinator(
        KEY_ID_BYTE,
        Repeat(SequenceCombinator(ZeroOrOne(WhitespaceTerminal()), KEY_ID_BYTE), 0, 15),
        EOF()
    )
)
