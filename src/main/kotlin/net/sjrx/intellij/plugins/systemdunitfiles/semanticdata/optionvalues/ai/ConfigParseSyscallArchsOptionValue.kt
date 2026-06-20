package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for SystemCallArchitectures=.
 *
 * C function: config_parse_syscall_archs in src/core/load-fragment.c → seccomp_arch_from_string
 * in src/shared/seccomp-util.c. Whitespace-separated list of architecture names — the table
 * below mirrors the streq checks in the C source, including arches gated by libseccomp build
 * options (loongarch64, riscv64): they're always accepted by the validator since users won't
 * know which seccomp build the host has, and the runtime check is what's authoritative anyway.
 */
class ConfigParseSyscallArchsOptionValue : SimpleGrammarOptionValues(
    "config_parse_syscall_archs",
    SequenceCombinator(
        ARCH,
        ZeroOrMore(SequenceCombinator(WhitespaceTerminal(), ARCH)),
        EOF()
    )
) {
    companion object {
        private val ARCH = LiteralChoiceTerminal(
            "native", "x86", "x86-64", "x32",
            "arm", "arm64",
            "loongarch64",
            "mips", "mips64", "mips64-n32", "mips-le", "mips64-le", "mips64-le-n32",
            "parisc", "parisc64",
            "ppc", "ppc64", "ppc64-le",
            "riscv64",
            "s390", "s390x"
        )
    }
}
