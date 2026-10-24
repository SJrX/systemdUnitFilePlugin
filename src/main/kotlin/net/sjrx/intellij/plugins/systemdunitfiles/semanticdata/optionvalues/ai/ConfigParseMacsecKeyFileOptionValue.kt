package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NON_API_VFS_ABSOLUTE_PATH
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [MACsecTransmitAssociation] / [MACsecReceiveAssociation] KeyFile= in a .netdev file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#KeyFile=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/netdev/macsec.c#L770-L821   config_parse_macsec_key_file (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L34-L88   path_simplify_and_warn (PATH_CHECK_ABSOLUTE|PATH_CHECK_NON_API_VFS)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/mountpoint-util.c#L721-L726   path_below_api_vfs
 */

/**
 * Validator for `KeyFile=` in both `[MACsecTransmitAssociation]` and `[MACsecReceiveAssociation]` (.netdev).
 *
 * C function: config_parse_macsec_key_file. The value is strdup'd verbatim — no specifier expansion and no unquoting, so
 * `%` and quotes are ordinary characters and a path may contain spaces — and checked with
 * path_simplify_and_warn(PATH_CHECK_ABSOLUTE|PATH_CHECK_NON_API_VFS): it must be absolute, must not keep
 * a `..` component after simplification, and must not be at or below `/dev`, `/sys` or `/proc`.
 * See [NON_API_VFS_ABSOLUTE_PATH]. An empty assignment clears the setting. An invalid value is logged
 * and ignored by systemd.
 */
class ConfigParseMacsecKeyFileOptionValue : SimpleGrammarOptionValues(
    "config_parse_macsec_key_file",
    SequenceCombinator(NON_API_VFS_ABSOLUTE_PATH, EOF())
)
