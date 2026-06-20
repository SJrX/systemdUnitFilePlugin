package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for LoadCredential= (ltype 0) and LoadCredentialEncrypted= (ltype 1).
 *
 * C function: config_parse_load_credential in src/core/load-fragment.c. The value is "<id>" or
 * "<id>:<source>": the id must satisfy credential_name_valid() (non-empty, not "."/"..", no '/',
 * no ':', no control, <= NAME_MAX); the optional source is either an absolute (normalized) path or
 * another credential name. The source is left loosely constrained (any non-control text) to avoid
 * false positives.
 *
 * Composed as id + optional(':' + source) so an invalid id localizes to the id.
 */
class ConfigParseLoadCredentialOptionValue : SimpleGrammarOptionValues(
    "config_parse_load_credential",
    SequenceCombinator(
        RegexTerminal("[^:]+", "(?!\\.\\.?(?::|$))[^/:\\x00-\\x1F]{1,255}"),
        ZeroOrOne(SequenceCombinator(LiteralChoiceTerminal(":"), RegexTerminal(".*", "[^\\x00-\\x1F]*"))),
        EOF()
    )
)
