package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for the .nspawn PrivateUsers= setting.
 *
 * C function: config_parse_private_users in src/nspawn/nspawn-settings.c. Accepts (in order
 * the C code checks):
 *   - boolean ("yes" / "no" / "1" / "0" / "true" / "false" / "on" / "off")
 *   - "pick" (random UID shift)
 *   - "identity" (UID shift 0, range 64K)
 *   - `uid` (single uint32, range defaults to 64K)
 *   - `uid:range` (two uint32 separated by colon)
 *
 * Alternatives are ordered from most-specific to least-specific so the colon form is tried
 * before the bare uid, and BOOLEAN (FlexibleLiteralChoiceTerminal) comes last — its syntactic
 * regex would otherwise greedily match tokens like "pick" or "identity" as boolean prefixes
 * and short-circuit AlternativeCombinator.
 */
class ConfigParsePrivateUsersOptionValue : SimpleGrammarOptionValues(
    "config_parse_private_users",
    SequenceCombinator(
        AlternativeCombinator(
            // uid:range
            SequenceCombinator(
                IntegerTerminal(0, 4_294_967_296L),
                LiteralChoiceTerminal(":"),
                IntegerTerminal(0, 4_294_967_296L)
            ),
            // bare uid
            IntegerTerminal(0, 4_294_967_296L),
            // string aliases. The unit-file PrivateUsers= (load-fragment.c, private_users_table: self, identity,
            // full, managed, or a boolean) shares this validator name with nspawn's, so both sets are accepted here.
            // https://github.com/systemd/systemd/blob/0f0682b773/src/core/namespace.c#L4263-L4271
            // https://github.com/systemd/systemd/blob/0f0682b773/src/nspawn/nspawn-settings.c#L662-L745
            LiteralChoiceTerminal("pick", "identity", "self", "full", "managed"),
            BOOLEAN
        ),
        EOF()
    )
)
