package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for IPIngressFilterPath= / IPEgressFilterPath=.
 *
 * C function: config_parse_ip_filter_bpf_progs in src/core/load-fragment.c. It runs
 * unit_path_printf() (specifier expansion) and then path_simplify_and_warn(PATH_CHECK_ABSOLUTE),
 * i.e. the resolved value must be an absolute path.
 *
 * Grammar: the value must start with '/' (an absolute path) or '%' (a specifier that may resolve
 * to an absolute path, e.g. %d/...); the remainder may be any non-control characters. Specifiers
 * are accepted loosely here (we do not enumerate the path-specifier set), since for a path the
 * meaningful, low-false-positive check is "must be absolute". The empty value resets the list.
 */
class ConfigParseIpFilterBpfProgsOptionValue : SimpleGrammarOptionValues(
    "config_parse_ip_filter_bpf_progs",
    SequenceCombinator(
        RegexTerminal(".+", "(?:/|%)[^\\x00-\\x1F]*"),
        EOF()
    )
)