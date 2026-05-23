package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for SystemCallErrorNumber=.
 *
 * C function: config_parse_syscall_errno in src/core/load-fragment.c → parse_errno in
 * src/basic/parse-util.c. Accepts:
 *   - "kill" (special, resets to SECCOMP_ERROR_NUMBER_KILL)
 *   - an errno name (uppercase "E"-prefixed, e.g. EPERM, ENOENT) via errno_from_name
 *   - an integer that maps to a valid errno via errno_is_valid (or 0)
 *
 * The grammar matches the errno-name pattern loosely (any uppercase E-prefixed token);
 * unknown names like "ENOIENT" would slip past the grammar but fail at runtime. Numeric
 * range here is 0..4095 — wider than typical valid errnos but enough to catch obvious typos.
 */
class ConfigParseSyscallErrnoOptionValue : SimpleGrammarOptionValues(
    "config_parse_syscall_errno",
    SequenceCombinator(
        AlternativeCombinator(
            LiteralChoiceTerminal("kill"),
            RegexTerminal("E[A-Z][A-Z0-9]*", "E[A-Z][A-Z0-9]*"),
            IntegerTerminal(0, 4096)
        ),
        EOF()
    )
)
