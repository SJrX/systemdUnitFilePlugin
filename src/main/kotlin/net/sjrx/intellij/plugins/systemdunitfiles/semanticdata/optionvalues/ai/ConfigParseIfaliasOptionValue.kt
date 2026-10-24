package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Link] Alias= in a .link file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.link.html#Alias=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/udev/net/link-config.c#L1944-L1980   config_parse_ifalias (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/include/uapi/linux/if.h#L33   IFALIASZ (256)
 */

/**
 * Validator for `[Link] Alias=` (.link).
 *
 * C function: config_parse_ifalias. The value is free text, but it must be ASCII (ascii_is_valid) and
 * shorter than IFALIASZ = 256 bytes, so at most 255 characters. Anything containing a non-ASCII
 * character (`Café uplink`) or longer than that is logged and ignored by udev. An empty assignment
 * clears the alias.
 */
class ConfigParseIfaliasOptionValue : SimpleGrammarOptionValues(
    "config_parse_ifalias",
    SequenceCombinator(RegexTerminal("[\\s\\S]+", "[\\x00-\\x7F]{1,255}"), EOF())
)
