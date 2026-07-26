package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.IntegerTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.unsignedNumber
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [Service] SuccessExitStatus=, RestartPreventExitStatus= and RestartForceExitStatus=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.service.html#SuccessExitStatus=
 * parser https://github.com/systemd/systemd/blob/a8e93919c3/src/core/load-fragment.c   config_parse_set_status
 * names  https://github.com/systemd/systemd/blob/a8e93919c3/src/shared/exit-status.c   exit_status_mappings, exit_status_from_string
 *        https://github.com/systemd/systemd/blob/a8e93919c3/src/basic/signal-util.c    static_signal_table, signal_from_string
 * bases  https://github.com/systemd/systemd/blob/a8e93919c3/src/basic/parse-util.h     safe_atou8 passes base 0 to strtoul
 */

/**
 * Validator for `[Service] SuccessExitStatus=`, `RestartPreventExitStatus=` and
 * `RestartForceExitStatus=`.
 *
 * C function: config_parse_set_status in src/core/load-fragment.c — a whitespace-separated list where
 * each word is resolved by exit_status_from_string first (src/shared/exit-status.c: a name from
 * exit_status_mappings, or a decimal 0…255 via safe_atou8) and, failing that, by signal_from_string
 * (src/basic/signal-util.c: a name from static_signal_table with or without the `SIG` prefix, or the
 * realtime forms `RTMIN[+n]` / `RTMAX[-n]`).
 *
 * A bare number therefore only ever means an exit status, so the numeric range is 0…255 rather than
 * the signal range — signal_from_string is reached with a number only when safe_atou8 already failed,
 * and every such number is outside SIGNAL_VALID.
 */
class ConfigParseSetStatusOptionValue : SimpleGrammarOptionValues(
    "config_parse_set_status",
    SequenceCombinator(
        STATUS,
        ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), STATUS)),
        EOF()
    )
) {
    companion object {

        /** Names from exit_status_mappings, plus every static_signal_table name bare and `SIG`-prefixed. */
        private val EXIT_STATUS_OR_SIGNAL_NAME = FlexibleLiteralChoiceTerminal(
            // exit_status_mappings — libc
            "SUCCESS", "FAILURE",
            // exit_status_mappings — systemd's private range
            "CHDIR", "NICE", "FDS", "EXEC", "MEMORY", "LIMITS", "OOM_ADJUST", "SIGNAL_MASK",
            "STDIN", "STDOUT", "CHROOT", "IOPRIO", "TIMERSLACK", "SECUREBITS", "SETSCHEDULER",
            "CPUAFFINITY", "GROUP", "USER", "CAPABILITIES", "CGROUP", "SETSID", "CONFIRM",
            "STDERR", "PAM", "NETWORK", "NAMESPACE", "NO_NEW_PRIVILEGES", "SECCOMP",
            "SELINUX_CONTEXT", "PERSONALITY", "APPARMOR", "ADDRESS_FAMILIES", "RUNTIME_DIRECTORY",
            "CHOWN", "SMACK_PROCESS_LABEL", "KEYRING", "STATE_DIRECTORY", "CACHE_DIRECTORY",
            "LOGS_DIRECTORY", "CONFIGURATION_DIRECTORY", "NUMA_POLICY", "CREDENTIALS", "BPF",
            "KSM", "MEMORY_THP", "EXCEPTION",
            // exit_status_mappings — LSB
            "INVALIDARGUMENT", "NOTIMPLEMENTED", "NOPERMISSION", "NOTINSTALLED", "NOTCONFIGURED",
            "NOTRUNNING",
            // exit_status_mappings — BSD
            "USAGE", "DATAERR", "NOINPUT", "NOUSER", "NOHOST", "UNAVAILABLE", "SOFTWARE", "OSERR",
            "OSFILE", "CANTCREAT", "IOERR", "TEMPFAIL", "PROTOCOL", "NOPERM", "CONFIG",
            // static_signal_table, bare
            "HUP", "INT", "QUIT", "ILL", "TRAP", "ABRT", "BUS", "FPE", "KILL", "USR1", "SEGV",
            "USR2", "PIPE", "ALRM", "TERM", "STKFLT", "CHLD", "CONT", "STOP", "TSTP", "TTIN",
            "TTOU", "URG", "XCPU", "XFSZ", "VTALRM", "PROF", "WINCH", "IO", "PWR", "SYS",
            // static_signal_table, with the SIG prefix signal_from_string strips
            "SIGHUP", "SIGINT", "SIGQUIT", "SIGILL", "SIGTRAP", "SIGABRT", "SIGBUS", "SIGFPE",
            "SIGKILL", "SIGUSR1", "SIGSEGV", "SIGUSR2", "SIGPIPE", "SIGALRM", "SIGTERM",
            "SIGSTKFLT", "SIGCHLD", "SIGCONT", "SIGSTOP", "SIGTSTP", "SIGTTIN", "SIGTTOU",
            "SIGURG", "SIGXCPU", "SIGXFSZ", "SIGVTALRM", "SIGPROF", "SIGWINCH", "SIGIO", "SIGPWR",
            "SIGSYS",
        )

        /**
         * `RTMIN`, `RTMIN+n`, `RTMAX`, `RTMAX-n`, each optionally `SIG`-prefixed. The offset bound is
         * SIGRTMAX - SIGRTMIN, which is 30 with glibc's reserved realtime signals.
         */
        private val REALTIME_SIGNAL = AlternativeCombinator(
            SequenceCombinator(
                LiteralChoiceTerminal("SIGRTMIN", "RTMIN"),
                ZeroOrOne(SequenceCombinator(LiteralChoiceTerminal("+"), IntegerTerminal(0, 31)))
            ),
            SequenceCombinator(
                LiteralChoiceTerminal("SIGRTMAX", "RTMAX"),
                ZeroOrOne(SequenceCombinator(LiteralChoiceTerminal("-"), IntegerTerminal(0, 31)))
            ),
        )

        // The number goes first so the name terminal's lenient shape match (its choices contain
        // digits, e.g. USR1) can't swallow a numeric word before the range check runs.
        private val STATUS = AlternativeCombinator(
            unsignedNumber(256),
            REALTIME_SIGNAL,
            EXIT_STATUS_OR_SIGNAL_NAME,
        )
    }
}
