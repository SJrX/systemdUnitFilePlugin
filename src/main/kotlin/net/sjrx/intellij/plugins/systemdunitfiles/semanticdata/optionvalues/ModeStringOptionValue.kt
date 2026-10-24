package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues

import com.intellij.openapi.project.Project
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.Validator

/**
 * Matches a mode value written in octal (UMask=, SocketMode=, DirectoryMode=, ...).
 *
 * [parse_mode](https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L90-L110) reads the value
 * with safe_atou_full(base 8, SAFE_ATO_REFUSE_PLUS_MINUS) and refuses anything above 07777. So any number of octal
 * digits is fine (`71`, `0`, `00022`) as long as the value is at most 7777; there is no 3 or 4 digit rule.
 * A sign, `0x`/`0o` prefixes and the digits 8 and 9 are refused.
 */
class ModeStringOptionValue : OptionValueInformation {
  override fun getAutoCompleteOptions(project: Project): Set<String> {
    return emptySet()
  }

  override fun getErrorMessage(value: String): String? {
    return if (value.matches("[0-7]+".toRegex()) && value.trimStart('0').length <= 4) {
      null
    } else {
      "Value is expected to be an octal number between 0 and 7777 not: $value"
    }
  }

  override val validatorName: String
    get() = VALIDATOR_NAME

  companion object {
    const val VALIDATOR_NAME = "config_parse_mode"

    val validators = mapOf(Validator(VALIDATOR_NAME, "0") to ModeStringOptionValue())
  }
}
