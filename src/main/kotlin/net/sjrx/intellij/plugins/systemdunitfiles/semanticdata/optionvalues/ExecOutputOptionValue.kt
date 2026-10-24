package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator

/**
 * StandardOutput= / StandardError=, config_parse_exec_output():
 * https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L1329-L1453
 */
class ExecOutputOptionValue() : OptionValueInformation {
  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return emptySet()
  }

  override fun getErrorMessage(value: String): String? {

    if (value.startsWith("fd:")) {
      // An empty name is fine; otherwise fdname_is_valid(): printable ASCII except ':', at most 255 characters.
      // https://github.com/systemd/systemd/blob/0f0682b773/src/basic/fd-util.c#L554-L579
      val name = value.removePrefix("fd:")
      if (name.length > FDNAME_MAX || name.any { it.code < 0x20 || it.code >= 0x7f || it == ':' }) {
        return "fd: name may only contain printable ASCII characters other than ':' and be at most $FDNAME_MAX characters long."
      }
      return null
    }

    if (value.startsWith("file:") || value.startsWith("append:") || value.startsWith("truncate:")) {
      // The path goes through path_simplify_and_warn(PATH_CHECK_ABSOLUTE|PATH_CHECK_FATAL): it must be absolute and
      // must not contain a ".." component (other than leading ones, which path_simplify() drops). A path that starts
      // with a specifier is only known once the specifier is expanded.
      // https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L34-L80
      val path = value.split(":", limit = 2)[1]
      if (path.isEmpty()) {
        return "path must be specified after the colon."
      }
      if (!ABSOLUTE_NORMALIZED_PATH.matches(path) && !path.startsWith("%")) {
        return "path after the colon must be an absolute, normalized path (no '..' components)."
      }
      return null
    }

    // syslog and syslog+console are obsolete, but systemd still accepts them (as journal and journal+console).
    // https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L1371-L1377
    // The rest is exec_output_table, https://github.com/systemd/systemd/blob/0f0682b773/src/core/execute.c#L3257-L3272
    when (value) {
      "inherit", "null", "tty", "journal", "kmsg", "journal+console", "kmsg+console", "socket", "fd", "file", "append", "truncate",
      "broker", "broker-log", "syslog", "syslog+console" -> return null
      else -> return "Takes one of inherit, null, tty, journal, kmsg, journal+console, kmsg+console, file:path, append:path, truncate:path, socket or fd:name."

    }
  }

  override val validatorName: String
    get() = VALIDATOR_NAME


  companion object {
    private val VALIDATOR_NAME = "config_parse_exec_output"

    private const val FDNAME_MAX = 255

    // An absolute path whose components after any leading "", "." and ".." ones (dropped by path_simplify()) are
    // never "..".
    private val ABSOLUTE_NORMALIZED_PATH = """/(?:\.{0,2}(?:/|$))*(?!(?:.*/)?\.\.(?:/|$)).*""".toRegex()

    val validators = mapOf(Validator(VALIDATOR_NAME, "0") to ExecOutputOptionValue())
  }
}
