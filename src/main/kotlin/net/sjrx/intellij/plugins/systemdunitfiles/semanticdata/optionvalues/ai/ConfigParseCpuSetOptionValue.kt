package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.CPU_SET
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Link] IRQAffinity= (.link) and [Exec] CPUAffinity= (.nspawn).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.link.html#IRQAffinity=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#CPUAffinity=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/cpu-set-util.c#L220-L307   config_parse_cpu_set (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L305-L340   parse_range
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478   safe_atou_full (base 0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/cpu-set-util.c#L186-L206   cpu_set_add_range (CPU_SET_MAX_NCPU)
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/test/test-cpu-set-util.c#L36   TEST(parse_cpu_set)
 * linux  https://man7.org/linux/man-pages/man2/sched_setaffinity.2.html
 */

/**
 * Validator for `[Link] IRQAffinity=` (.link) and `[Exec] CPUAffinity=` (.nspawn).
 *
 * C function: config_parse_cpu_set. CPU indices and `N-M` ranges, separated by any mix of whitespace
 * and commas (separators coalesce; leading/trailing ones are fine), each word optionally quoted. Each
 * bound is a safe_atou() number (so hex works) below CPU_SET_MAX_NCPU = 8192. See [CPU_SET].
 *
 * Lenient on one point: a reversed range such as `3-0` is rejected by systemd after parsing (it
 * compares the numbers); that is not modelled. An empty assignment clears the set.
 */
class ConfigParseCpuSetOptionValue : SimpleGrammarOptionValues(
    "config_parse_cpu_set",
    SequenceCombinator(CPU_SET, EOF())
)
