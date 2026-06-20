package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for User= / Group= (and similar) keys parsed by config_parse_user_group_compat.
 *
 * C function: config_parse_user_group_compat in src/core/load-fragment.c. It runs unit_full_printf()
 * (specifier expansion) and then valid_user_group_name() with
 * VALID_USER_ALLOW_NUMERIC | VALID_USER_RELAX | VALID_USER_WARN.
 *
 * In RELAX mode (see src/basic/user-util.c) the name is checked only superficially: it must be
 * non-empty, valid UTF-8, contain no control characters, contain no ':' or '/', and have no
 * leading/trailing whitespace (interior whitespace is allowed). Numeric IDs are allowed. We model
 * exactly those rules and leave the rarer RELAX checks (purely-numeric-but-not-a-uid, "-N", "."/
 * "..") unenforced rather than risk false positives. '%' is allowed (specifier syntax). The empty
 * value resets the field.
 */
class ConfigParseUserGroupCompatOptionValue : SimpleGrammarOptionValues(
    "config_parse_user_group_compat",
    SequenceCombinator(
        RegexTerminal(".+", "[^\\s:/\\x00-\\x1F](?:[^:/\\x00-\\x1F]*[^\\s:/\\x00-\\x1F])?"),
        EOF()
    )
)
