package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.LiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.RegexTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/*
 * [Unit] ConditionFirmware= / AssertFirmware=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionFirmware=
 * parser https://github.com/systemd/systemd/blob/048970650c/src/core/load-fragment.c  config_parse_unit_condition_string (CONDITION_FIRMWARE)
 * check  https://github.com/systemd/systemd/blob/048970650c/src/shared/condition.c    condition_test_firmware
 */

/**
 * Validator for `[Unit] ConditionFirmware=` / `AssertFirmware=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_FIRMWARE. condition_test_firmware
 * (src/shared/condition.c) recognises exactly four forms and reports "unsupported" for anything else:
 *
 *  - `uefi`
 *  - `device-tree`
 *  - `device-tree-compatible(DTC)`  — the parser takes everything up to the last `)`, which must end the value
 *  - `smbios-field(FIELD OP VALUE)` — likewise closed by the last `)`
 *
 * The two parenthesised forms are matched as `keyword(` + argument + `)`; the argument is modelled loosely
 * (any non-empty run without `)`), which covers every real device-tree compatible string and SMBIOS field
 * expression. The rare case of a `)` inside the argument (the parser's strrchr() would keep it) is not
 * modelled, matching the plugin's preference for never flagging a value systemd accepts over covering an
 * exotic spelling.
 */
class ConfigParseUnitConditionFirmwareOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(
        AlternativeCombinator(
            SequenceCombinator(
                LiteralChoiceTerminal("device-tree-compatible("),
                RegexTerminal("[^)]+", "[^)]+"),
                LiteralChoiceTerminal(")"),
            ),
            SequenceCombinator(
                LiteralChoiceTerminal("smbios-field("),
                RegexTerminal("[^)]+", "[^)]+"),
                LiteralChoiceTerminal(")"),
            ),
            FlexibleLiteralChoiceTerminal("device-tree", "uefi"),
        )
    )
)
