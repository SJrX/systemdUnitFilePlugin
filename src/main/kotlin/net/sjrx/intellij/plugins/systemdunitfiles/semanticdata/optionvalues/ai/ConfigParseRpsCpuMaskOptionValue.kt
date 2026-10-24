package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.AlternativeCombinator
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.CPU_SET
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.EOF
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.SequenceCombinator

/*
 * [Link] ReceivePacketSteeringCPUMask= in a .link file.
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.link.html#ReceivePacketSteeringCPUMask=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/udev/net/link-config.c#L2095-L2136   config_parse_rps_cpu_mask (0)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/shared/cpu-set-util.c#L220-L307   config_parse_cpu_set
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/25-rps-cpu-multi.link#L6-L7   ReceivePacketSteeringCPUMask=0 1 / =2,3
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/test-network/conf/24-rps-cpu-invalid.link#L6   ReceivePacketSteeringCPUMask=0 3 8-invalid (deliberately invalid)
 */

/**
 * Validator for `[Link] ReceivePacketSteeringCPUMask=` (.link).
 *
 * C function: config_parse_rps_cpu_mask. The whole value `disable` (an empty mask) or `all` (every
 * online CPU), compared with streq, or else a CPU set exactly as config_parse_cpu_set reads it — see
 * [CPU_SET]. An empty assignment clears the mask. An invalid word is logged and skipped by systemd.
 */
class ConfigParseRpsCpuMaskOptionValue : SimpleGrammarOptionValues(
    "config_parse_rps_cpu_mask",
    AlternativeCombinator(
        SequenceCombinator(FlexibleLiteralChoiceTerminal("disable", "all"), EOF()),
        SequenceCombinator(CPU_SET, EOF()),
    )
)
