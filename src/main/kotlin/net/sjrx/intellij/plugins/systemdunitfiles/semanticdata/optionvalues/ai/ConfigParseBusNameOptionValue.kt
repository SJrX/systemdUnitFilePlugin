package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BatchUnitCoreGrammar.withUnitSpecifiers
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.DOT
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.OneOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Service] BusName=
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.service.html#BusName=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L2421-L2453   config_parse_bus_name (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/unit-printf.c#L179   unit_full_printf_full (specifiers expanded first)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/libsystemd/sd-bus/bus-type.c#L144   sd_bus_service_name_is_valid
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/libsystemd/sd-bus/bus-internal.c#L119   service_name_is_valid
 * linux  https://dbus.freedesktop.org/doc/dbus-specification.html#message-protocol-names-bus
 */

// One dot-separated element of a well-known name: letters, digits, `_` and `-`, not starting with a
// digit. The syntactic shape is wider so a digit-led element is reported as the bad token.
private val WELL_KNOWN_ELEMENT = RegexTerminal("[A-Za-z0-9_-]+", "[A-Za-z_-][A-Za-z0-9_-]*")

// In a unique name (leading `:`) elements may start with a digit.
private val UNIQUE_ELEMENT = RegexTerminal("[A-Za-z0-9_-]+", "[A-Za-z0-9_-]+")

/**
 * Validator for `[Service] BusName=` (config_parse_bus_name, the only key using it).
 *
 * After specifier expansion the name must pass sd_bus_service_name_is_valid(): two or more
 * dot-separated elements of `[A-Za-z0-9_-]`, none empty and none starting with a digit — or a unique
 * name (`:1.42`), whose elements may start with a digit; systemd accepts that form too, odd as it is
 * for this setting. The 255-character limit is not checked. Any value that uses a known unit
 * specifier (`org.example.%i`) is accepted.
 */
class ConfigParseBusNameOptionValue : SimpleGrammarOptionValues(
  "config_parse_bus_name",
  withUnitSpecifiers(
    AlternativeCombinator(
      SequenceCombinator(LiteralChoiceTerminal(":"), UNIQUE_ELEMENT, OneOrMore(SequenceCombinator(DOT, UNIQUE_ELEMENT)), EOF()),
      SequenceCombinator(WELL_KNOWN_ELEMENT, OneOrMore(SequenceCombinator(DOT, WELL_KNOWN_ELEMENT)), EOF()),
    )
  )
)
