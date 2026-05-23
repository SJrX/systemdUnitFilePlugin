package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for nanosecond-time options such as TimerSlackNSec=.
 *
 * C function: defined via DEFINE_PARSER(nsec, ..., parse_nsec) in src/shared/conf-parser.c,
 * which delegates to parse_nsec in src/basic/time-util.c. parse_nsec accepts "infinity" or
 * one or more numeric terms with optional unit suffix (s/ms/us/ns/min/h/d/w/M/y...). The same
 * syntax that TIME_VALUE encodes for the parse_sec family applies here; only the default
 * (suffix-less) multiplier differs.
 */
class ConfigParseNsecOptionValue : SimpleGrammarOptionValues(
    "config_parse_nsec",
    SequenceCombinator(
        OptionalWhitespacePrefix(TIME_VALUE),
        EOF()
    )
)
