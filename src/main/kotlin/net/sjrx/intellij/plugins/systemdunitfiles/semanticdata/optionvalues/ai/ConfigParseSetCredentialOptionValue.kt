package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for SetCredential= (ltype 0) and SetCredentialEncrypted= (ltype 1).
 *
 * C function: config_parse_set_credential in src/core/load-fragment.c. The value is "<id>:<data>":
 * extract_first_word(sep ":") yields the id, which must satisfy credential_name_valid()
 * (= filename_is_valid && fdname_is_valid, i.e. non-empty, not "."/"..", no '/', no ':', no
 * control characters, <= NAME_MAX); the remainder is the credential data (an arbitrary
 * C-unescaped string for ltype 0, Base64 for ltype 1). The data itself is left unconstrained (so
 * invalid Base64 is a false-negative rather than a false-positive). The empty value resets the list.
 *
 * Composed as id + ':' + data so an invalid id localizes to the id rather than the whole value.
 */
class ConfigParseSetCredentialOptionValue : SimpleGrammarOptionValues(
    "config_parse_set_credential",
    SequenceCombinator(
        RegexTerminal("[^:]+", "(?!\\.\\.?:)[^/:\\x00-\\x1F]{1,255}"),
        LiteralChoiceTerminal(":"),
        RegexTerminal(".*", ".*"),
        EOF()
    )
)
