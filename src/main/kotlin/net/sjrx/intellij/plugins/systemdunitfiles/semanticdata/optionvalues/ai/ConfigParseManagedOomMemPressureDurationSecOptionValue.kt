package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.*
import java.math.BigInteger

/**
 * Validator for ManagedOOMMemoryPressureDurationSec=.
 *
 * C function: config_parse_managed_oom_mem_pressure_duration_sec in src/core/load-fragment.c. The empty value
 * resets it. Otherwise parse_sec(rvalue) must succeed and the result must be at least 1s and not infinity.
 */
class ConfigParseManagedOomMemPressureDurationSecOptionValue : SimpleGrammarOptionValues(
    "config_parse_managed_oom_mem_pressure_duration_sec",
    SequenceCombinator(
        // parse_sec(), then at least 1s and not infinity.
        // https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L4134-L4170
        ValueChecked(parseSec(maxUsec = SafeAtoTerminal.UINT64_MAX - BigInteger.ONE)) { text ->
            evaluateParseSec(text)?.let { it >= BigInteger.valueOf(1_000_000L) }
        },
        EOF()
    )
)
