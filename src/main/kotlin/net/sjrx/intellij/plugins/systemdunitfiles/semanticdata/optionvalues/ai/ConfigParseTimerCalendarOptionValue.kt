package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BatchUnitCoreGrammar.CALENDAR_SPEC
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BatchUnitCoreGrammar.withUnitSpecifiers

/*
 * [Timer] OnCalendar=
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.timer.html#OnCalendar=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.time.html#Calendar%20Events
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L2043-L2114   config_parse_timer (TIMER_CALENDAR)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/unit-printf.c#L179   unit_full_printf_full (specifiers expanded first)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/calendarspec.c#L870-L1103   calendar_spec_from_string_full
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/calendarspec.c#L375   parse_weekdays
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/calendarspec.c#L719   parse_date
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/calendarspec.c#L802   parse_calendar_time
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/calendarspec.c#L619   prepend_component
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/calendarspec.c#L500   parse_component_decimal
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/calendarspec.c#L125   fix_year
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/calendarspec.c#L169   chain_valid
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/calendarspec.c#L212   calendar_spec_valid
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1596   verify_timezone
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-calendarspec.c#L117   calendar_spec_one (accepted spellings)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-calendarspec.c#L231   calendar_spec_from_string (rejected spellings)
 */

/**
 * Validator for `[Timer] OnCalendar=`, the only key using config_parse_timer with TIMER_CALENDAR.
 *
 * After unit_full_printf() the value goes to calendar_spec_from_string(), which accepts either a
 * shorthand keyword (`daily`, `weekly`, `semi-annually`, … — any case) or
 *
 *     [WEEKDAYS] [[YEAR-]MONTH-DAY | [YEAR-]MONTH~DAY | @EPOCH] [HOUR:MINUTE[:SECOND]]
 *
 * optionally followed by a timezone (` UTC`, the local zone abbreviation or an IANA zone name). Each
 * date/time field is `*` or a comma list of `value[..value][/repeat]`; seconds may carry fractions.
 * See [CALENDAR_SPEC] for exactly which per-field bounds are checked and which cross-field checks
 * (reversed ranges, overshooting repetitions, descending weekday ranges, zone existence) are left to
 * systemd. A value that uses a unit specifier is accepted as long as the specifier is a known one.
 *
 * An empty assignment resets the timer list and is handled before this grammar is consulted.
 */
class ConfigParseTimerCalendarOptionValue : SimpleGrammarOptionValues(
  "config_parse_timer",
  withUnitSpecifiers(CALENDAR_SPEC)
)
