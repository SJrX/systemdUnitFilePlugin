package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.ValueWarning
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.ValueWarnings
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for User= / Group= (and similar) keys parsed by config_parse_user_group_compat.
 *
 * C function: config_parse_user_group_compat,
 * https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L2550-L2597. It runs
 * unit_full_printf() (specifier expansion) and then valid_user_group_name() with
 * VALID_USER_ALLOW_NUMERIC | VALID_USER_RELAX | VALID_USER_WARN
 * (https://github.com/systemd/systemd/blob/0f0682b773/src/basic/user-util.c#L718-L791):
 *   - something parse_uid() accepts is fine: decimal, no sign, no leading zero, below 2^32-1, and not 65535;
 *   - otherwise the RELAX checks apply: no leading/trailing space, no control characters, no ':' or '/', not
 *     all digits, not '-' followed by only digits (so not "-1000" and not "-"), and not "." or "..".
 * '%' is allowed (specifier syntax), but an unknown specifier (e.g. %Q) makes unit_full_printf() fail
 * (https://github.com/systemd/systemd/blob/0f0682b773/src/shared/specifier.c#L33-L105). The empty value resets the
 * field.
 */
class ConfigParseUserGroupCompatOptionValue : SimpleGrammarOptionValues(
    "config_parse_user_group_compat",
    SequenceCombinator(
        PreferValidAlternativeCombinator(
            RegexTerminal(
                ".+",
                "(?=${BatchUnitCoreGrammar.UNIT_TEXT_CHAR}*\$)(?!-?[0-9]*$)(?!\\.\\.?$)[^\\s:/\\x00-\\x1F\\x7F](?:[^:/\\x00-\\x1F\\x7F]*[^\\s:/\\x00-\\x1F\\x7F])?"
            ),
            // uid_is_valid() also refuses 65535, the 16-bit -1.
            ValueChecked(SafeAtoTerminal(
                signed = false,
                base = 10,
                flags = setOf(
                    SafeAtoTerminal.Flag.REFUSE_PLUS_MINUS,
                    SafeAtoTerminal.Flag.REFUSE_LEADING_ZERO,
                    SafeAtoTerminal.Flag.REFUSE_LEADING_WHITESPACE
                ),
                minInclusive = 0L,
                maxInclusive = 4_294_967_294L,
                acceptsWhitespace = false,
                acceptsMinus = true,
            )) { text -> text != "65535" },
        ),
        EOF()
    )
),
    ValueWarnings {

    /**
     * For a key whose name contains "User" (User=, SocketUser=) the literal name "nobody" is accepted with the notice
     * "Special user nobody configured, this is not safe!". Group=nogroup, Group=nobody and the numeric 65534 pass
     * silently. https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L2587-L2594
     */
    override fun valueWarnings(key: String, value: String): List<ValueWarning> {
        if (!key.contains("User") || value != "nobody") return emptyList()
        return listOf(
            ValueWarning(
                0, value.length,
                "$key=nobody is not safe: the service shares every file and process owned by nobody, including files " +
                    "whose owner cannot be mapped on NFS or in user namespaces. Use DynamicUser=yes or a dedicated system " +
                    "user instead."
            )
        )
    }
}
