package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.ValueWarning
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.ValueWarnings
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BatchUnitCoreGrammar

/**
 * StandardOutput= / StandardError=, config_parse_exec_output():
 * https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L1329-L1453
 */
class ExecOutputOptionValue() : OptionValueInformation, ValueWarnings {
  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return emptySet()
  }

  override fun getErrorMessage(value: String): String? {

    if (value.startsWith("fd:")) {
      // An empty name is fine; otherwise fdname_is_valid(): printable ASCII except ':', at most 255 characters.
      // https://github.com/systemd/systemd/blob/0f0682b773/src/basic/fd-util.c#L554-L579
      // The name goes through unit_fd_printf() first, so an unknown specifier (%Q) is refused.
      // https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L1354-L1367
      val name = value.removePrefix("fd:")
      if (!KNOWN_SPECIFIERS_ONLY.matches(name)) {
        return "fd: name contains an unknown specifier."
      }
      if (name.length > FDNAME_MAX || name.any { it.code < 0x20 || it.code >= 0x7f || it == ':' }) {
        return "fd: name may only contain printable ASCII characters other than ':' and be at most $FDNAME_MAX characters long."
      }
      return null
    }

    if (value.startsWith("file:") || value.startsWith("append:") || value.startsWith("truncate:")) {
      // The path goes through path_simplify_and_warn(PATH_CHECK_ABSOLUTE|PATH_CHECK_FATAL): it must be absolute and
      // must not contain a ".." component (other than leading ones, which path_simplify() drops). It goes through
      // unit_path_printf() first: an unknown specifier (%Q) is refused, "%%" is a literal '%', and a path that starts
      // with a known specifier, or uses one later on, is only known once the specifier is expanded.
      // https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L34-L80
      // https://github.com/systemd/systemd/blob/0f0682b773/src/shared/specifier.c#L33-L105
      val path = value.split(":", limit = 2)[1]
      if (path.isEmpty()) {
        return "path must be specified after the colon."
      }
      if (!KNOWN_SPECIFIERS_ONLY.matches(path)) {
        return "path after the colon contains an unknown specifier."
      }
      if (!ABSOLUTE_NORMALIZED_PATH.matches(path) && !SPECIFIED_ABSOLUTE_PATH.matches(path)) {
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

  /**
   * syslog and syslog+console are rewritten to journal and journal+console with an "obsolete" notice.
   * https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L1371-L1377
   * https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L1428-L1431
   */
  override fun valueWarnings(key: String, value: String): List<ValueWarning> {
    val replacement = OBSOLETE_OUTPUTS[value] ?: return emptyList()
    return listOf(ValueWarning(0, value.length, "$key=$value is obsolete; systemd treats it as $replacement. Use $replacement instead.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING))
  }

  override val validatorName: String
    get() = VALIDATOR_NAME


  companion object {
    private val VALIDATOR_NAME = "config_parse_exec_output"

    private const val FDNAME_MAX = 255

    private val OBSOLETE_OUTPUTS = mapOf("syslog" to "journal", "syslog+console" to "journal+console")

    // An absolute path whose components after any leading "", "." and ".." ones (dropped by path_simplify()) are
    // never "..".
    private val ABSOLUTE_NORMALIZED_PATH = """/(?:\.{0,2}(?:/|$))*(?!(?:.*/)?\.\.(?:/|$))(?:[^%]|%%|%(?![A-Za-z0-9%]))*""".toRegex()

    // Text whose every '%' is "%%", a known specifier, or a '%' that specifier_printf() keeps as it is.
    private val KNOWN_SPECIFIERS_ONLY = "${BatchUnitCoreGrammar.UNIT_TEXT_CHAR}*".toRegex()

    // A path that starts with a known specifier, or starts with '/' and uses a known specifier later on.
    private val SPECIFIED_ABSOLUTE_PATH =
      "(?:%[${BatchUnitCoreGrammar.UNIT_SPECIFIER_LETTERS}]|/(?=(?:[^%]|%%|%(?![A-Za-z0-9%]))*%[${BatchUnitCoreGrammar.UNIT_SPECIFIER_LETTERS}])).*"
        .toRegex()

    val validators = mapOf(Validator(VALIDATOR_NAME, "0") to ExecOutputOptionValue())
  }
}
