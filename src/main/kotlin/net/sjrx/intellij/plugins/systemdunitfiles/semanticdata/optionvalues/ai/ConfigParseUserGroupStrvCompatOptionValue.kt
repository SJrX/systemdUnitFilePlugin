package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for SupplementaryGroups= (and any other key parsed by config_parse_user_group_strv_compat).
 *
 * C function: config_parse_user_group_strv_compat in src/core/load-fragment.c → valid_user_group_name
 * with VALID_USER_ALLOW_NUMERIC | VALID_USER_RELAX | VALID_USER_WARN. The relaxed mode accepts a wide
 * range of user/group names because real-world auth stacks (SSSD, Samba, …) are permissive. Per
 * src/basic/user-util.c the constraints are:
 *   - non-empty
 *   - no leading/trailing whitespace
 *   - no colons (/etc/passwd field separator), no slashes
 *   - no control characters
 *   - not fully numeric (UID is allowed via numeric parse, but mixed numeric+text isn't)
 *   - not "." / ".."
 *
 * Pre-expansion, words may also contain "%X" specifiers (unit_full_printf expands before the
 * validity check), so the grammar tolerates "%" inline.
 */
class ConfigParseUserGroupStrvCompatOptionValue : SimpleGrammarOptionValues(
    "config_parse_user_group_strv_compat",
    SequenceCombinator(
        NAME,
        ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), NAME)),
        EOF()
    )
) {
    companion object {
        // No colon, slash, whitespace, or shell metacharacters. Length unbounded in relaxed mode.
        // Negative lookahead excludes "." and ".." as literal entries.
        private val NAME = RegexTerminal(
            "(?!\\.{1,2}\\Z)[^\\s:/]+",
            "(?!\\.{1,2}\\Z)[^\\s:/]+"
        )
    }
}
