package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ADDRESS_FAMILY_NAME
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrMore
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ZeroOrOne

/*
 * [Exec] RestrictAddressFamilies= (.nspawn).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#RestrictAddressFamilies=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd-nspawn.html#--restrict-address-families=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-settings.c#L1068-L1095   config_parse_restrict_address_families (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L90-L145   parse_address_families
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/extract-word.c#L10-L188   extract_first_word (WHITESPACE ",", EXTRACT_UNQUOTE)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/af-list.c#L39-L51   af_from_name
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/meson.build#L168   gperf --ignore-case for the af lookup table
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-13-NSPAWN.nspawn.sh#L412-L419
 * linux  https://man7.org/linux/man-pages/man7/address_families.7.html
 */

/** One or more separators: parse_address_families splits on whitespace and commas, coalescing runs. */
private val SEPARATORS = RegexTerminal("""[\s,]+""", """[\s,]+""")

/** One family name, optionally in single or double quotes (EXTRACT_UNQUOTE strips them). */
private val FAMILY_WORD = AlternativeCombinator(
    SequenceCombinator(LiteralChoiceTerminal("'"), ADDRESS_FAMILY_NAME, LiteralChoiceTerminal("'")),
    SequenceCombinator(LiteralChoiceTerminal("\""), ADDRESS_FAMILY_NAME, LiteralChoiceTerminal("\"")),
    ADDRESS_FAMILY_NAME,
)

/**
 * Validator for the .nspawn `[Exec] RestrictAddressFamilies=` setting
 * (config_parse_restrict_address_families, ltype 0), which hands the value to the same
 * parse_address_families() helper as the unit-file RestrictAddressFamilies=.
 *
 * At 0f0682b773 that helper accepts:
 *
 *  - exactly `none` (allow nothing);
 *  - an optional leading `~` (deny-list), immediately followed by a list of family names separated by
 *    whitespace and/or commas (runs coalesce; leading/trailing separators are fine; a bare `~` is an
 *    empty deny-list). Words are unquoted, so `'AF_INET'` is fine.
 *
 * Names go through af_from_name(), whose gperf table is case-insensitive and which retries with `AF_`
 * prepended: `AF_INET`, `af_inet`, `inet` and `INET6` all resolve. Kernel-removed families (AF_DECnet,
 * AF_IRDA, AF_ECONET, AF_WANPIPE) still resolve and are marked deprecated. An unknown name is logged and
 * the rest of the line ignored.
 */
class ConfigParseRestrictAddressFamiliesOptionValue : SimpleGrammarOptionValues(
    "config_parse_restrict_address_families",
    AlternativeCombinator(
        SequenceCombinator(LiteralChoiceTerminal("none"), EOF()),
        SequenceCombinator(
            ZeroOrOne(LiteralChoiceTerminal("~")),
            ZeroOrMore(SEPARATORS),
            ZeroOrOne(SequenceCombinator(FAMILY_WORD, ZeroOrMore(SequenceCombinator(SEPARATORS, FAMILY_WORD)))),
            ZeroOrMore(SEPARATORS),
            EOF(),
        ),
    )
)
