package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.ID128_URN_PREFIX
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Labeled
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NONZERO_ID128
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.NONZERO_ID128_UUID
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.Role
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Exec] MachineID= (.nspawn).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#MachineID=
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd-nspawn.html#--uuid=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/shared/conf-parser.c#L1190-L1218   config_parse_id128 (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/libsystemd/sd-id128/id128-util.c#L25-L40   id128_from_string_nonzero
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/libsystemd/sd-id128/sd-id128.c#L60-L112   sd_id128_from_string
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/src/libsystemd/sd-id128/test-id128.c#L68-L97
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-13-NSPAWN.nspawn.sh#L522
 */

/**
 * Validator for the .nspawn `[Exec] MachineID=` setting (config_parse_id128, ltype 0; no other gperf
 * table uses this parser).
 *
 * id128_from_string_nonzero() hands the value to sd_id128_from_string(), which accepts either 32 hex
 * digits or the dashed 8-4-4-4-12 UUID form (hex digits in any case), optionally behind a
 * case-insensitive `urn:uuid:` prefix — which then requires the dashed form. The all-zero ID is
 * refused (logged and ignored).
 */
class ConfigParseId128OptionValue : SimpleGrammarOptionValues(
    "config_parse_id128",
    SequenceCombinator(
        Labeled(
            Role.LITERAL,
            AlternativeCombinator(
                SequenceCombinator(ID128_URN_PREFIX, NONZERO_ID128_UUID),
                NONZERO_ID128,
            ),
        ),
        EOF(),
    )
)
