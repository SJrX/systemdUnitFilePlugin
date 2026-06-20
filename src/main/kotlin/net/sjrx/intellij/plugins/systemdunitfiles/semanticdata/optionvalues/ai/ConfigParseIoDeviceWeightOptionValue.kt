package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for IODeviceWeight=.
 *
 * C function: config_parse_io_device_weight in src/core/load-fragment.c. The value is
 * "<path> <weight>": the first whitespace-separated word is a device path (specifier-expanded,
 * path_simplify_and_warn with flags 0 -- NOT required to be absolute), and the remainder is parsed
 * by cg_weight_parse(), which accepts an integer in [CGROUP_WEIGHT_MIN, CGROUP_WEIGHT_MAX] =
 * [1, 10000] (src/basic/cgroup-util.h).
 *
 * Grammar: a non-whitespace path token, whitespace, then an integer 1..10000. The path is left
 * unconstrained (any token) to match the lenient C parsing and avoid false positives.
 */
class ConfigParseIoDeviceWeightOptionValue : SimpleGrammarOptionValues(
    "config_parse_io_device_weight",
    SequenceCombinator(
        RegexTerminal("\\S+", "\\S+"),
        WhitespaceTerminal(),
        IntegerTerminal(1, 10001),
        EOF()
    )
)
