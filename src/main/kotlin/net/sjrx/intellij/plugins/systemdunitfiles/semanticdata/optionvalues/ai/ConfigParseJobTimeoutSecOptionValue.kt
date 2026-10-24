package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BatchUnitCoreGrammar.TIMESPAN

/*
 * [Unit] JobTimeoutSec=
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#JobTimeoutSec=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L5594-L5630   config_parse_job_timeout_sec (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1293   parse_sec_fix_0 (parse_sec, then 0 means infinity)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1198-L1287   parse_time
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/time-util.c#L1147   extract_multiplier
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-time-util.c#L85   TEST(parse_sec_fix_0)
 */

/**
 * Validator for `[Unit] JobTimeoutSec=` (config_parse_job_timeout_sec, the only key using it).
 *
 * The value goes straight to parse_sec_fix_0() — no specifier expansion — so it is a plain
 * [TIMESPAN]: `infinity`, `0` (which this parser also turns into infinity), `90`, `5min 20s`, `1h30min`.
 *
 * There is no special case for an empty value in the C parser: parse_sec("") fails and the assignment
 * is ignored with a warning. The plugin does not run value validators on empty assignments, so that
 * case is not reported here.
 */
class ConfigParseJobTimeoutSecOptionValue : SimpleGrammarOptionValues(
  "config_parse_job_timeout_sec",
  TIMESPAN
)
