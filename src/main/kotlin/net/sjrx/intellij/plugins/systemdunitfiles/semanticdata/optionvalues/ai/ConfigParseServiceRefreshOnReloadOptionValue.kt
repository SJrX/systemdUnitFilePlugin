package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.BOOLEAN
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.WhitespaceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [Service] RefreshOnReload=
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.service.html#RefreshOnReload=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L4928-L4975   config_parse_service_refresh_on_reload (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L23-L46   parse_boolean (STRCASE_IN_SET: any case)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/service.c#L6630-L6657   service_refresh_on_reload_from_string_many
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/service.c#L6615   service_refresh_on_reload_table ("extensions", "credentials"; streq, so case-sensitive)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10   extract_first_word (whitespace-separated, flags 0)
 */

private val REFRESH_RESOURCE = FlexibleLiteralChoiceTerminal("extensions", "credentials")
private val OPTIONAL_WHITESPACE = ZeroOrOne(WhitespaceTerminal())

/**
 * Validator for `[Service] RefreshOnReload=` (config_parse_service_refresh_on_reload, the only key using
 * it).
 *
 * Either a boolean in any case (parse_boolean is tried first and is case-insensitive, so `Yes` and
 * `TRUE` are fine), or a whitespace-separated list of `extensions` and
 * `credentials`, optionally prefixed by a single `~` to invert it. A bare `~` is accepted by systemd
 * (an empty inverted list, i.e. the default) and so is accepted here. A boolean cannot be inverted
 * (`~yes` is an unknown resource name).
 *
 * Not modelled: extract_first_word() (flags 0) still decodes a backslash escape, so the obfuscated
 * spelling `ext\ensions` is accepted by systemd but flagged here.
 *
 * An empty assignment resets to the default and is handled before this grammar is consulted.
 */
class ConfigParseServiceRefreshOnReloadOptionValue : SimpleGrammarOptionValues(
  "config_parse_service_refresh_on_reload",
  AlternativeCombinator(
    SequenceCombinator(BOOLEAN, EOF()),
    SequenceCombinator(
      ZeroOrOne(LiteralChoiceTerminal("~")),
      OPTIONAL_WHITESPACE,
      ZeroOrOne(
        SequenceCombinator(REFRESH_RESOURCE, ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), REFRESH_RESOURCE)), OPTIONAL_WHITESPACE)
      ),
      EOF(),
    ),
  )
)
