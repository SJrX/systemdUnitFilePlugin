package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.safeAtoNumber

/*
 * [Exec] OOMScoreAdjust= (.nspawn).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#OOMScoreAdjust=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd-nspawn.html#--oom-score-adjust=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-settings.c#L792-L828   config_parse_oom_score_adjust (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L762-L777   parse_oom_score_adjust
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L497-L520   safe_atoi
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L403-L427   mangle_base
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/process-util.c#L1934-L1936   oom_score_adjust_is_valid
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-13-NSPAWN.nspawn.sh#L529
 * linux  https://man7.org/linux/man-pages/man5/proc_pid_oom_score_adj.5.html
 */

/**
 * Validator for the .nspawn `[Exec] OOMScoreAdjust=` setting (config_parse_oom_score_adjust, ltype 0).
 *
 * parse_oom_score_adjust() reads the value with safe_atoi() — base 0 after mangle_base(), so besides
 * plain decimal it takes a sign, `0x` hex, leading-zero octal, `0o` octal and `0b` binary (where whitespace and a
 * sign may follow the prefix, as in `0b-1` or `0b 1`, because strtol() skips them) — and then requires OOM_SCORE_ADJ_MIN…OOM_SCORE_ADJ_MAX, i.e. -1000…1000 inclusive. An empty value resets the
 * setting and never reaches the grammar. Out-of-range or malformed input is logged and ignored.
 */
class ConfigParseOomScoreAdjustOptionValue : SimpleGrammarOptionValues(
    "config_parse_oom_score_adjust",
    SequenceCombinator(safeAtoNumber(-1000, 1001, spaceAfterPrefix = true), EOF()),
)
