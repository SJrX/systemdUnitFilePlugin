package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.openapi.project.Project

/**
 * This validator is used for the config_parse_documentation validator.
 *
 *
 * While this is only use for one key, it is fairly popular in the source code, so we implemented it.
 *
 *
 * Source code is from
 *
 *
 * load-fragment.c ~ line 2600
 * web-util.c ~ line
 *
 */
class DocumentationOptionValue : OptionValueInformation {
  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return emptySet()
  }

  override fun getErrorMessage(value: String): String? {

    // This loose validation is what systemd does
    for (valueTerm in value.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
      if (valueTerm.startsWith("http://")) {
        continue
      }
      if (valueTerm.startsWith("https://")) {
        continue
      }
      if (valueTerm.startsWith("file:/")) {
        continue
      }
      if (valueTerm.startsWith("info:")) {
        continue
      }
      if (valueTerm.startsWith("man:")) {
        continue
      }
      return "Documentation $valueTerm does not match expected syntax, each value should be a space separated list beginning with (http://, https://, file:/, info:, man:)"
    }
    return null
  }

  override val validatorName: String
    get() = "config_parse_documentation"
}
