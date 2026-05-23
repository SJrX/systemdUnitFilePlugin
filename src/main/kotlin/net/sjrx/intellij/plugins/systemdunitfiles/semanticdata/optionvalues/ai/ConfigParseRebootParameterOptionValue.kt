package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for RebootArgument= / RebootParameter= keys.
 *
 * C function: config_parse_reboot_parameter in src/core/load-fragment.c → reboot_parameter_is_valid
 * in src/shared/reboot-util.c. After unit_full_printf specifier expansion, the result must:
 *   - pass ascii_is_valid (printable ASCII)
 *   - have length ≤ NAME_MAX (255)
 *
 * Grammar allows printable ASCII (0x20..0x7E) including "%" for specifier syntax; length cap
 * of 255 is enforced via a regex quantifier.
 */
class ConfigParseRebootParameterOptionValue : SimpleGrammarOptionValues(
    "config_parse_reboot_parameter",
    SequenceCombinator(
        RegexTerminal("[\\x20-\\x7E]{1,255}", "[\\x20-\\x7E]{1,255}"),
        EOF()
    )
)
