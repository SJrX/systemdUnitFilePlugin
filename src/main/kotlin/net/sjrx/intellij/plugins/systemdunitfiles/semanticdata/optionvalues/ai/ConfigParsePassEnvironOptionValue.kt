package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for PassEnvironment=.
 *
 * C function: config_parse_pass_environ in src/core/load-fragment.c. Whitespace-separated list
 * of environment variable names. Each word is first run through unit_env_printf (resolves
 * %p / %n / etc.), then env_name_is_valid: must not be empty, must not start with a digit,
 * and may contain only characters in VALID_BASH_ENV_NAME_CHARS (alphanumerics and underscore).
 *
 * The grammar tolerates "%X" specifiers inline by including "%" in the allowed-character set,
 * since the C parser resolves them before the validity check. This means a few unresolved
 * specifier-heavy inputs that the runtime parser would reject will pass here — preferred over
 * false positives for legitimate `PassEnvironment=%n_LOG` style usage.
 */
class ConfigParsePassEnvironOptionValue : SimpleGrammarOptionValues(
    "config_parse_pass_environ",
    SequenceCombinator(
        RegexTerminal("[A-Za-z_%][A-Za-z0-9_%]*", "[A-Za-z_%][A-Za-z0-9_%]*"),
        ZeroOrMore(SequenceCombinator(
            WhitespaceTerminal(),
            RegexTerminal("[A-Za-z_%][A-Za-z0-9_%]*", "[A-Za-z_%][A-Za-z0-9_%]*")
        )),
        EOF()
    )
)
