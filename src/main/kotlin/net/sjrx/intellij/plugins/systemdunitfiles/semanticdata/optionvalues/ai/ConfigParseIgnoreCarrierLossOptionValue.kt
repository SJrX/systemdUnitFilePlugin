package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BOOLEAN_ANY_CASE
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.PARSE_SEC_TIMESPAN

/*
 * [Network] IgnoreCarrierLoss= (.network).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#IgnoreCarrierLoss=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/network/networkd-network.c#L1096-L1138  config_parse_ignore_carrier_loss (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L23-L46             parse_boolean
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1289-L1291          parse_sec
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1198-L1287          parse_time
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-time-util.c#L21-L82        TEST(parse_sec)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-bridge99-ignore-carrier-loss.network#L9
 */

/**
 * Validator for `[Network] IgnoreCarrierLoss=`: a boolean (case-insensitive; yes = infinity, no = 0), or
 * a time span as parse_sec() reads it (`5s`, `1min 30s`, `1h30min`, `.5s`, `infinity`) — see
 * [PARSE_SEC_TIMESPAN].
 */
class ConfigParseIgnoreCarrierLossOptionValue : SimpleGrammarOptionValues(
    "config_parse_ignore_carrier_loss",
    AlternativeCombinator(
        SequenceCombinator(BOOLEAN_ANY_CASE, EOF()),
        SequenceCombinator(PARSE_SEC_TIMESPAN, EOF()),
    )
)
