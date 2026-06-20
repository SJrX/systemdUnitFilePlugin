package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for IODeviceLatencyTargetSec=.
 *
 * C function: config_parse_io_device_latency in src/core/load-fragment.c. The value is
 * "<path> <latency>": the first whitespace-separated word is a device path (specifier-expanded,
 * path_simplify_and_warn with flags 0 -- not required to be absolute), and the remainder is a
 * time span parsed by parse_sec().
 *
 * Grammar: a non-whitespace path token, whitespace, then a time span: one or more
 * "<number><optional unit>" components (units per systemd time syntax), or "infinity". The number
 * may be fractional. The path is left unconstrained to match the lenient C parsing.
 */
class ConfigParseIoDeviceLatencyOptionValue : SimpleGrammarOptionValues(
    "config_parse_io_device_latency",
    SequenceCombinator(
        RegexTerminal("\\S+", "\\S+"),
        WhitespaceTerminal(),
        RegexTerminal(
            ".+",
            "infinity|(?:\\d+(?:\\.\\d+)?\\s*(?:nsec|ns|usec|us|msec|ms|seconds|second|sec|s|minutes|minute|min|hours|hour|hr|h|days|day|d|weeks|week|w|months|month|M|years|year|y|m)?\\s*)+"
        ),
        EOF()
    )
)
