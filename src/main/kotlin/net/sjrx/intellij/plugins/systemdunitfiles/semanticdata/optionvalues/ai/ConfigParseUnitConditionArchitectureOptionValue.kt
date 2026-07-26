package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.conditionString

/*
 * [Unit] ConditionArchitecture= / AssertArchitecture=.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.unit.html#ConditionArchitecture=
 * parser https://github.com/systemd/systemd/blob/a8e93919c3/src/core/load-fragment.c   config_parse_unit_condition_string (CONDITION_ARCHITECTURE)
 * check  https://github.com/systemd/systemd/blob/a8e93919c3/src/shared/condition.c     condition_test_architecture_parameter
 * values https://github.com/systemd/systemd/blob/a8e93919c3/src/basic/architecture.c   architecture_table
 */

/**
 * Validator for `[Unit] ConditionArchitecture=` / `AssertArchitecture=`.
 *
 * C function: config_parse_unit_condition_string with ltype CONDITION_ARCHITECTURE; the parameter is
 * checked by condition_test_architecture_parameter (src/shared/condition.c), which accepts the literal
 * `native` or any name in architecture_table (src/basic/architecture.c).
 */
class ConfigParseUnitConditionArchitectureOptionValue : SimpleGrammarOptionValues(
    "config_parse_unit_condition_string",
    conditionString(ARCHITECTURE)
) {
    companion object {
        private val ARCHITECTURE = FlexibleLiteralChoiceTerminal(
            "native",
            "alpha",
            "arc",
            "arc-be",
            "arm",
            "arm-be",
            "arm64",
            "arm64-be",
            "cris",
            "ia64",
            "loongarch64",
            "m68k",
            "mips",
            "mips-le",
            "mips64",
            "mips64-le",
            "nios2",
            "parisc",
            "parisc64",
            "ppc",
            "ppc-le",
            "ppc64",
            "ppc64-le",
            "riscv32",
            "riscv64",
            "s390",
            "s390x",
            "sh",
            "sh64",
            "sparc",
            "sparc64",
            "tilegx",
            "x86",
            "x86-64",
        )
    }
}
