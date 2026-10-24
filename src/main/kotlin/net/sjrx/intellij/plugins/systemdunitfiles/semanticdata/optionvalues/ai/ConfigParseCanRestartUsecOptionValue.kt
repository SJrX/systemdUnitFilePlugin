package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NETWORK_ROUTE_ADDR_USEC_INFINITY
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.USEC_PER_MSEC
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.divRoundUp
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.timeSpan
import java.math.BigInteger

/*
 * [CAN] RestartSec= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#RestartSec=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-can.c#L210-L245   config_parse_can_restart_usec (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1198-L1287    parse_time (via parse_sec)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1147-L1196    extract_multiplier
 */

private val UINT32_MAX: BigInteger = BigInteger.valueOf(4_294_967_295L)

/**
 * Validator for `[CAN] RestartSec=` (.network).
 *
 * config_parse_can_restart_usec reads a time span with parse_sec() and ignores it unless it is
 * `infinity` or rounds up to at most UINT32_MAX milliseconds (the kernel's IFLA_CAN_RESTART_MS is a
 * u32). `0` (disable automatic restart) is accepted. Note this parser has no isempty() branch of its
 * own; an empty assignment is not something this grammar is asked about.
 */
class ConfigParseCanRestartUsecOptionValue : SimpleGrammarOptionValues(
    "config_parse_can_restart_usec",
    SequenceCombinator(timeSpan { it == NETWORK_ROUTE_ADDR_USEC_INFINITY || divRoundUp(it, USEC_PER_MSEC) <= UINT32_MAX }, EOF())
)
