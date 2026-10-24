package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BatchUnitCoreGrammar.TIMESPAN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BatchUnitCoreGrammar.withUnitSpecifiers

/*
 * [Timer] OnActiveSec= / OnBootSec= / OnStartupSec= / OnUnitActiveSec= / OnUnitInactiveSec=
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.timer.html#OnActiveSec=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.time.html#Parsing%20Time%20Spans
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L2043-L2114   config_parse_timer (TIMER_ACTIVE, TIMER_BOOT, TIMER_STARTUP, TIMER_UNIT_ACTIVE, TIMER_UNIT_INACTIVE)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/unit-printf.c#L179   unit_full_printf_full (specifiers expanded first)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1198-L1287   parse_time (via parse_sec, default unit seconds)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1147   extract_multiplier
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-time-util.c#L21   TEST(parse_sec)
 */

/**
 * Validator for the five monotonic `[Timer]` settings — `OnActiveSec=`, `OnBootSec=`,
 * `OnStartupSec=`, `OnUnitActiveSec=` and `OnUnitInactiveSec=`. They share config_parse_timer, whose
 * non-calendar branch hands the specifier-expanded value to parse_sec().
 *
 * Accepted: `infinity`, or one or more `number[unit]` elements — `50`, `5min`, `1h30min`, `5s 500ms`,
 * `.5y`, `+3.1s`, `3.1s.2` — exactly as described for [TIMESPAN]; any value using a known unit
 * specifier. Refused: negative numbers, a dangling `.` (`5.`), unknown or mis-pluralised units
 * (`5mins`, `2hs`) and two bare numbers glued together (`12.34.56`).
 *
 * An empty assignment resets the timer list and is handled before this grammar is consulted.
 */
class ConfigParseTimerMonotonicOptionValue : SimpleGrammarOptionValues(
  "config_parse_timer",
  withUnitSpecifiers(TIMESPAN)
)
