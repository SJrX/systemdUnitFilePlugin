package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for SystemCallErrorNumber=.
 *
 * C function: config_parse_syscall_errno → parse_errno,
 * https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L3504-L3541
 * https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L342-L360. Accepts:
 *   - "kill" (special, resets to SECCOMP_ERROR_NUMBER_KILL)
 *   - an errno name (E-prefixed, any case, e.g. EPERM, eperm, E2BIG) via errno_from_name
 *   - a safe_atoi() number from 1 to ERRNO_MAX (4095). parse_errno() lets 0 through, but
 *     config_parse_syscall_errno() then refuses it ("Invalid error number").
 *
 * The grammar matches the errno-name pattern loosely (any E-prefixed alphanumeric token);
 * unknown names like "ENOIENT" would slip past the grammar.
 */
class ConfigParseSyscallErrnoOptionValue : SimpleGrammarOptionValues(
    "config_parse_syscall_errno",
    SequenceCombinator(
        AlternativeCombinator(
            LiteralChoiceTerminal("kill"),
            // errno_from_name() is a gperf lookup built with --ignore-case, so "eperm" is fine, and names such as
            // E2BIG have a digit second.
            // https://github.com/systemd/systemd/blob/0f0682b773/src/basic/errno-list.c#L14-L25
            // https://github.com/systemd/systemd/blob/0f0682b773/src/basic/meson.build#L164-L173
            RegexTerminal("[Ee][A-Za-z0-9]+", "[Ee][A-Za-z0-9]+"),
            SafeAtoTerminal(
                signed = true,
                base = 0,
                flags = emptySet(),
                minInclusive = 1L,
                maxInclusive = 4095L,
                acceptsWhitespace = true,
                acceptsMinus = true,
            ),
        ),
        EOF()
    )
)
