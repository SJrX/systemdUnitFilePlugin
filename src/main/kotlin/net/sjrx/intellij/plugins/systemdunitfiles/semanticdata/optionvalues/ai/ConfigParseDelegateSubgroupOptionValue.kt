package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for DelegateSubgroup=.
 *
 * C function: config_parse_delegate_subgroup in src/core/load-fragment.c. The value is used
 * verbatim (no specifier expansion) and must satisfy !cg_needs_escape() (src/basic/cgroup-util.c):
 * it must be a valid filename (no '/', not "."/"..", <= NAME_MAX), must not start with '_' or '.',
 * must not be one of "notify_on_release"/"release_agent"/"tasks", and must not start with
 * "cgroup.".
 *
 * Grammar models all of those except the rarer "<controller>." rule (e.g. "cpu.") which we leave
 * unenforced to avoid coupling to the kernel controller list / false positives. First character
 * must not be '/', '.', '_' or a control char; subsequent characters must not be '/' or control;
 * length <= 255.
 */
class ConfigParseDelegateSubgroupOptionValue : SimpleGrammarOptionValues(
    "config_parse_delegate_subgroup",
    SequenceCombinator(
        RegexTerminal(
            ".+",
            "(?!cgroup\\.)(?!(?:tasks|release_agent|notify_on_release)$)[^/._\\x00-\\x1F][^/\\x00-\\x1F]{0,254}"
        ),
        EOF()
    )
)
