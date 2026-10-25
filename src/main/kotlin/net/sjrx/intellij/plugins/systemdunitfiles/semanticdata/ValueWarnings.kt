package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata

/**
 * A part of a value that systemd accepts but warns about (#565): an obsolete spelling it rewrites, a setting it calls
 * unsafe, or a form it silently corrects. [start, end) is the span inside the value; [message] says what systemd does
 * with it and what to write instead.
 */
data class ValueWarning(val start: Int, val end: Int, val message: String)

/**
 * Implemented by option validators that know values systemd accepts with a warning. The ValueWarning inspection
 * reports what [valueWarnings] returns.
 *
 * Contract: return warnings only for values the validator otherwise accepts, so a warning never piles on an error.
 */
interface ValueWarnings {
  fun valueWarnings(key: String, value: String): List<ValueWarning>
}

/**
 * patch_var_run(): an absolute path whose first two components are `var` and `run` is rewritten to /run/ with a
 * notice. path_startswith() skips empty and "." components, so `//var/./run/x` counts as well.
 * https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L573-L601
 */
fun legacyVarRunWarning(key: String, value: String): ValueWarning? {
  if (!value.startsWith("/")) return null
  val components = value.split('/').filter { it.isNotEmpty() && it != "." }
  if (components.size < 2 || components[0] != "var" || components[1] != "run") return null
  val replacement = "/run/" + components.drop(2).joinToString("/")
  return ValueWarning(
    0,
    value.length,
    "$key=$value is below the legacy directory /var/run/; systemd rewrites it to $replacement and logs a notice. " +
      "Use $replacement instead."
  )
}

/**
 * config_parse_in_addr_prefix() with the "warn" ltype (Address= and Peer= in [Network]/[Address]): an address without
 * "/N" is accepted with the full-width prefix and a warning.
 * https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L2114-L2149
 */
fun missingPrefixLengthWarning(key: String, value: String): ValueWarning? {
  if (value.contains('/')) return null
  val prefix = if (value.contains(':')) 128 else 32
  return ValueWarning(
    0,
    value.length,
    "$key=$value has no prefix length; systemd assumes /$prefix and logs a warning. Write the prefix length explicitly, " +
      "e.g. $value/$prefix for a single address or the length of the subnet."
  )
}
