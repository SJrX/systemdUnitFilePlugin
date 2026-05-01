package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues

/**
 * Validator for config_parse_fdname(0).
 * Used by Options: Socket.FileDescriptorName.
 *
 * Mirrors fdname_is_valid:
 *   - Printable ASCII only: bytes >= 0x20 (space) and < 0x7F (DEL).
 *   - The ':' character (0x3A) is reserved as a separator in $LISTEN_FDNAMES and is forbidden.
 *   - Maximum length is 255 characters (FDNAME_MAX). The empty string is handled by the
 *     caller (it clears the value) and is not validated here.
 */
class ConfigParseFdnameOptionValue : SimpleGrammarOptionValues(
    "config_parse_fdname",
    SequenceCombinator(
        RegexTerminal("[ -9;-~]{1,255}", "[ -9;-~]{1,255}"),
        EOF()
    )
)
