package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SafeAtoTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Link] IRQAffinityNUMA= in a .link file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.link.html#IRQAffinityNUMA=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/udev/net/link-config.c#L2157-L2201   config_parse_irq_affinity_numa (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/udev/net/link-config.h#L35-L36   IRQ_AFFINITY_NUMA_UNSET / IRQ_AFFINITY_NUMA_LOCAL
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/basic/parse-util.c#L429-L478   safe_atou_full (base 0)
 */

/**
 * Validator for `[Link] IRQAffinityNUMA=` (.link).
 *
 * C function: config_parse_irq_affinity_numa. Either `local` (the device's own NUMA node) or a NUMA
 * node number read with safe_atou() (so hex works). UINT_MAX and UINT_MAX - 1 are reserved as the
 * "unset" and "local" markers, so the number must be at most 4294967293. An empty assignment unsets it.
 * An out-of-range number is logged and ignored by udev.
 */
class ConfigParseIrqAffinityNumaOptionValue : SimpleGrammarOptionValues(
    "config_parse_irq_affinity_numa",
    SequenceCombinator(
        AlternativeCombinator(
            FlexibleLiteralChoiceTerminal("local"),
            SafeAtoTerminal(
                signed = false,
                base = 0,
                flags = emptySet(),
                minInclusive = 0L,
                maxInclusive = 4_294_967_293L,
                acceptsWhitespace = true,
                acceptsMinus = true,
            ),
        ),
        EOF()
    )
)
