package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
import java.math.BigInteger

/**
 * config_parse_signal(): signal_from_string() must return a signal number above 0.
 *
 * https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L1556-L1581
 * https://github.com/systemd/systemd/blob/0f0682b773/src/basic/signal-util.c#L164-L224
 */
class SignalOptionValue : OptionValueInformation {

  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return SIGNALS
  }

  override fun getErrorMessage(value: String): String? {
    val trimmed = value.trim()

    // A number (safe_atoi) must satisfy SIGNAL_VALID: 1 ≤ n < _NSIG (65),
    // https://github.com/systemd/systemd/blob/0f0682b773/src/basic/signal-util.h#L56-L58
    val number = SIGNAL_NUMBER.valueOf(trimmed)
    if (number != null) {
      if (number >= BigInteger.ONE && number <= BigInteger.valueOf(SIGRTMAX.toLong())) {
        return null
      }
      return "Illegal signal value of ${value}, if using an integer the value must be between 1 and $SIGRTMAX."
    }

    val name = trimmed.removePrefix("SIG")
    if ("SIG$name" in SIGNALS) {
      return null
    }

    // RTMIN, RTMIN+n, RTMAX, RTMAX-n with 0 ≤ n ≤ SIGRTMAX-SIGRTMIN; the sign is part of what safe_atoi() parses.
    if (name == "RTMIN" || name == "RTMAX") {
      return null
    }
    if (name.startsWith("RTMIN+")) {
      val offset = SIGNAL_NUMBER.valueOf(name.removePrefix("RTMIN"))
      if (offset != null && offset >= BigInteger.ZERO && offset <= BigInteger.valueOf((SIGRTMAX - SIGRTMIN).toLong())) {
        return null
      }
    }
    if (name.startsWith("RTMAX-")) {
      val offset = SIGNAL_NUMBER.valueOf(name.removePrefix("RTMAX"))
      if (offset != null && offset <= BigInteger.ZERO && offset >= BigInteger.valueOf((SIGRTMIN - SIGRTMAX).toLong())) {
        return null
      }
    }

    return """Illegal signal value of ${value}, please consult the signal(7) man page for a full list."""
  }

  override val validatorName: String
    get() = VALIDATOR_NAME
  companion object {
    const val VALIDATOR_NAME = "config_parse_signal"

    // glibc on Linux: SIGRTMIN is 34 (the first two real-time signals are reserved by glibc), SIGRTMAX is 64.
    private const val SIGRTMIN = 34
    private const val SIGRTMAX = 64

    // safe_atoi(): signed, base 0, so "+5" and "0x9" are numbers too.
    private val SIGNAL_NUMBER = SafeAtoTerminal(
      signed = true,
      base = 0,
      flags = emptySet(),
      minInclusive = SafeAtoTerminal.INT32_MIN,
      maxInclusive = SafeAtoTerminal.INT32_MAX,
      acceptsWhitespace = true,
      acceptsMinus = true,
    )

    // static_signal_table, https://github.com/systemd/systemd/blob/0f0682b773/src/basic/signal-util.c#L110-L144
    // (SIGCLD, SIGEMT, SIGINFO, SIGIOT, SIGLOST and SIGPOLL are not in it, so systemd rejects them).
    private val SIGNALS = setOf("SIGABRT", "SIGALRM", "SIGBUS", "SIGCHLD", "SIGCONT", "SIGFPE", "SIGHUP", "SIGILL", "SIGINT", "SIGIO",
                                "SIGKILL", "SIGPIPE", "SIGPROF", "SIGPWR", "SIGQUIT", "SIGSEGV", "SIGSTKFLT", "SIGSTOP", "SIGSYS",
                                "SIGTERM", "SIGTRAP", "SIGTSTP", "SIGTTIN", "SIGTTOU", "SIGURG", "SIGUSR1", "SIGUSR2", "SIGVTALRM",
                                "SIGWINCH", "SIGXCPU", "SIGXFSZ")

    val validators = mapOf(Validator(VALIDATOR_NAME, "0") to SignalOptionValue())
  }
}
