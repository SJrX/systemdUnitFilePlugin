package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*

/**
 * Validator for CPUSetPartition=.
 *
 * C function: config_parse_cpuset_partition, expanded via
 * DEFINE_CONFIG_PARSE_ENUM_WITH_DEFAULT in src/core/cgroup-util.c. The accepted values are
 * exactly the entries of cpuset_partition_table in src/core/cgroup.c:
 *   - member
 *   - root
 *   - isolated
 *
 * Empty also resolves (to the default _CPUSET_PARTITION_INVALID), but the inspector won't be
 * called on an empty value in any case.
 */
class ConfigParseCpusetPartitionOptionValue : SimpleGrammarOptionValues(
    "config_parse_cpuset_partition",
    SequenceCombinator(
        LiteralChoiceTerminal("member", "root", "isolated"),
        EOF()
    )
)
