package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator

class SignalOptionValue : OptionValueInformation {

  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return SIGNALS
  }

  override fun getErrorMessage(value: String): String? {
    if (value.trim() in SIGNALS) {
      return null
    }

    val valInt = value.toIntOrNull()

    if (valInt is Int) {
      if (valInt in 0..63) {
        return null;
      } else {
        return "Illegal signal value of ${value}, if using an integer the value must be between 0 and 63."
      }
    }

    if ("SIG${value}" in SIGNALS) {
      return null;
    }

    return """Illegal signal value of ${value}, please consult the signal(7) man page for a full list."""
  }

  override val validatorName: String
    get() = VALIDATOR_NAME
  companion object {
    const val VALIDATOR_NAME = "config_parse_signal"

    private val SIGNALS = setOf("SIGABRT","SIGALRM","SIGBUS","SIGCHLD","SIGCLD","SIGCONT","SIGEMT","SIGFPE","SIGHUP","SIGILL","SIGINFO","SIGINT","SIGIO","SIGIOT","SIGKILL","SIGLOST","SIGPIPE","SIGPOLL","SIGPROF","SIGPWR","SIGQUIT","SIGSEGV","SIGSTKFLT","SIGSTOP","SIGSYS","SIGTERM","SIGTRAP","SIGTSTP","SIGTTIN","SIGTTOU","SIGURG","SIGUSR1","SIGUSR2","SIGVTALRM","SIGWINCH","SIGXCPU","SIGXFSZ")

    val validators = mapOf(Validator(VALIDATOR_NAME, "0") to SignalOptionValue())
  }
}

