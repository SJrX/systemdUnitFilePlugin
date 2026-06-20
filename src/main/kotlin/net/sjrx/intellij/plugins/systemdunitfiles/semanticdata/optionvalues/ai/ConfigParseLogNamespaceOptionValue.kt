package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for LogNamespace=.
 *
 * C function: config_parse_log_namespace in src/core/load-fragment.c -> log_namespace_name_valid()
 * in src/basic/syslog-util.c. After specifier expansion the value must be string_is_safe with
 * STRING_FILENAME semantics (no '/', not "."/"..", no control characters), a valid unit instance
 * name, and <= LOG_NAMESPACE_MAX characters.
 *
 * Grammar: a single token of non-control, non-'/' characters (not "."/".."), <= 255 chars; '%'
 * is allowed for specifier syntax. The empty value resets the field.
 */
class ConfigParseLogNamespaceOptionValue : SimpleGrammarOptionValues(
    "config_parse_log_namespace",
    SequenceCombinator(
        RegexTerminal(".+", "(?!\\.\\.?$)[^/\\x00-\\x1F]{1,255}"),
        EOF()
    )
)
