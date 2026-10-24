package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for RestrictAddressFamilies=.
 *
 * C function: config_parse_address_families → parse_address_families,
 * https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L3543-L3576
 * https://github.com/systemd/systemd/blob/0f0682b773/src/shared/parse-helpers.c#L90-L145. Accepts:
 *   - "none" (clears the set, sets allowlist)
 *   - an optional leading "~" (denylist), then words separated by whitespace and/or commas
 *     (extract_first_word with WHITESPACE "," and EXTRACT_UNQUOTE; leading, trailing and repeated separators are
 *     fine), each resolved by af_from_name(), which ignores case and retries with "AF_" prepended
 *     (https://github.com/systemd/systemd/blob/0f0682b773/src/basic/af-list.c#L39-L51). So `AF_INET`, `inet` and
 *     `AF_DECNET` are all fine. A word may be quoted.
 *
 * The name terminal is the shared [ADDRESS_FAMILY_NAME], which also marks the families the kernel removed as
 * deprecated.
 */
class ConfigParseAddressFamiliesOptionValue : SimpleGrammarOptionValues(
    "config_parse_address_families",
    SequenceCombinator(
        AlternativeCombinator(
            LiteralChoiceTerminal("none"),
            SequenceCombinator(
                ZeroOrOne(LiteralChoiceTerminal("~")),
                ZeroOrOne(RegexTerminal("[\\s,]+", "[\\s,]+")),
                ZeroOrOne(
                    SequenceCombinator(
                        AlternativeCombinator(
                            ADDRESS_FAMILY_NAME,
                            SequenceCombinator(LiteralChoiceTerminal("\""), ADDRESS_FAMILY_NAME, LiteralChoiceTerminal("\"")),
                            SequenceCombinator(LiteralChoiceTerminal("'"), ADDRESS_FAMILY_NAME, LiteralChoiceTerminal("'")),
                        ),
                        ZeroOrMore(
                            SequenceCombinator(
                                RegexTerminal("[\\s,]+", "[\\s,]+"),
                                AlternativeCombinator(
                                    ADDRESS_FAMILY_NAME,
                                    SequenceCombinator(LiteralChoiceTerminal("\""), ADDRESS_FAMILY_NAME, LiteralChoiceTerminal("\"")),
                                    SequenceCombinator(LiteralChoiceTerminal("'"), ADDRESS_FAMILY_NAME, LiteralChoiceTerminal("'")),
                                ),
                            )
                        ),
                        ZeroOrOne(RegexTerminal("[\\s,]+", "[\\s,]+")),
                    )
                ),
            )
        ),
        EOF()
    )
)
