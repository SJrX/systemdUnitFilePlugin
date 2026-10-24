package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [DHCPServer] BootFilename=, [MobileNetwork] APN= / OperatorId= / User= / Password= / PIN= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#BootFilename=
 *        https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#APN=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L1253-L1300   config_parse_string (CONFIG_PARSE_STRING_SAFE_AND_ASCII)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/string-util.c#L1154-L1198   string_is_safe (STRING_ALLOW_GLOBS)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/utf8.h#L17                  ascii_is_valid
 */

/**
 * Validator for config_parse_string with CONFIG_PARSE_STRING_SAFE_AND_ASCII
 * (= CONFIG_PARSE_STRING_SAFE | CONFIG_PARSE_STRING_ASCII): `[DHCPServer] BootFilename=` and the
 * `[MobileNetwork]` `APN=`, `OperatorId=`, `User=`, `Password=` and `PIN=` settings of .network files.
 *
 * Both checks run on the raw value. string_is_safe(…, STRING_ALLOW_GLOBS) refuses control characters
 * (0x01–0x1F, 0x7F), backslashes and the quote characters `"` and `'` (globs `*?[` are fine); and
 * ascii_is_valid() refuses any byte of 0x80 or above. Either failure logs a warning and ignores the
 * assignment. What remains is printable ASCII, space included, minus `"`, `'` and `\`.
 */
class ConfigParseStringSafeAsciiOptionValue : SimpleGrammarOptionValues(
    "config_parse_string",
    SequenceCombinator(
        RegexTerminal("""[\s\S]+""", """[\x20-\x7E&&[^"'\\]]+"""),
        EOF()
    )
)
