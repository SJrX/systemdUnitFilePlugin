package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.COLON
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.safeAtoNumber

/*
 * [Network] Port= (.nspawn).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#Port=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd-nspawn.html#--port=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-settings.c#L248-L274   config_parse_expose_port (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-expose-ports.c#L17-L73   expose_port_parse
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L718-L734   parse_ip_port
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478   safe_atou_full (base 0, SAFE_ATO_REFUSE_LEADING_WHITESPACE)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-13-NSPAWN.nspawn.sh#L557-L560
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-13-NSPAWN.nspawn.sh#L315-L326   (accepted and rejected --port= spellings)
 */

/**
 * parse_ip_port(): a safe_atou16() number (base 0) that is not zero. Whitespace between a `0b`/`0o` prefix and
 * the digits is skipped by strtoul() (`Port=0o 120` is valid), but not in front of the number (`80: 90` is not).
 */
private val PORT = safeAtoNumber(1, 65536, spaceAfterPrefix = true)

/**
 * Validator for the .nspawn `[Network] Port=` setting (config_parse_expose_port, ltype 0).
 *
 * expose_port_parse() strips an optional `tcp:` or `udp:` prefix (case-sensitive startswith(); the
 * default is TCP), then splits the rest at the first `:` into a host port and a container port. With no
 * colon the one number is used for both. Each port goes through parse_ip_port(): safe_atou16() with
 * base 0 (so `0x50` is port 80) and no leading whitespace, then 1…65535. A malformed or duplicate
 * specification is logged and ignored.
 */
class ConfigParseExposePortOptionValue : SimpleGrammarOptionValues(
    "config_parse_expose_port",
    SequenceCombinator(
        ZeroOrOne(SequenceCombinator(LiteralChoiceTerminal("tcp", "udp"), COLON)),
        PORT,
        ZeroOrOne(SequenceCombinator(COLON, PORT)),
        EOF(),
    )
)
