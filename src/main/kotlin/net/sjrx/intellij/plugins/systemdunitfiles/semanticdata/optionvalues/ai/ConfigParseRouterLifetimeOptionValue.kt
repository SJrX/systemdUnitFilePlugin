package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.timeSpan

/*
 * [IPv6SendRA] RouterLifetimeSec= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#RouterLifetimeSec=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-radv.c#L1463-L1512   config_parse_router_lifetime (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1198-L1287      parse_time (via parse_sec)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1147-L1196      extract_multiplier
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/libsystemd-network/radv-internal.h#L38-L39   RADV_MIN/MAX_ROUTER_LIFETIME_USEC
 */

/**
 * Validator for `[IPv6SendRA] RouterLifetimeSec=` (.network).
 *
 * config_parse_router_lifetime reads a time span with parse_sec(). Only a parse failure ignores the
 * assignment: `0` is kept (the host is not a default router), and any other value outside
 * RADV_MIN_ROUTER_LIFETIME_USEC…9000s — `infinity` included — is *clamped* into that range with a
 * warning, not rejected. So every parse_sec() spelling is accepted here; the clamping is not flagged.
 */
class ConfigParseRouterLifetimeOptionValue : SimpleGrammarOptionValues(
    "config_parse_router_lifetime",
    SequenceCombinator(timeSpan(), EOF())
)
