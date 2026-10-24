package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.bpfDelegateList

/*
 * BPFDelegatePrograms= ([Service]/[Socket]/[Mount]/[Swap]).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.exec.html#BPFDelegatePrograms=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L180   config_parse_bpf_delegate_programs (0), via DEFINE_CONFIG_PARSE_PTR
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/namespace.h#L279   bpf_delegate_programs_from_string
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/namespace.c#L4221-L4253   bpf_delegate_from_string ("any", or a "," list; unknown names warned and ignored)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/namespace.c#L4196   DEFINE_STRING_TABLE_LOOKUP(bpf_delegate_prog_type) — exact streq
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/generate-bpf-delegate-configs.py   builds bpf_delegate_prog_type_table from enum bpf_prog_type in src/include/uapi/linux/bpf.h
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-07-PID1.private-bpf.sh#L51   BPFDelegatePrograms=BPFProgTypeTracepoint,BPFProgTypeXdp,BPFProgTypeTracing
 * linux  https://man7.org/linux/man-pages/man2/bpf.2.html
 */

/**
 * Validator for `BPFDelegatePrograms=` (the bpffs `delegate_progs=` mount option; only effective with `PrivateBPF=yes`).
 *
 * Either `any` (the whole value) or a comma-separated list of the names below — see [bpfDelegateList]
 * for the splitting rules (commas only, no spaces; empty fields are skipped). The names are what
 * src/core/generate-bpf-delegate-configs.py makes of each `BPF_…` enumerator of `enum bpf_prog_type` in the
 * uapi bpf.h systemd bundles at 0f0682b773 (`BPF_FOO_BAR` → `BPFFooBar`; aliases written as
 * `X = Y,` are skipped by its regex and therefore not accepted). Regenerate with
 *
 *     python3 src/core/generate-bpf-delegate-configs.py code src/include/uapi/linux/bpf.h
 *
 * An unknown name is logged ("Unknown BPF delegate option, ignoring") and dropped, so it is flagged.
 * The list tracks the bundled kernel header, so a newer systemd may know names this one does not.
 *
 * Quirk faithfully reproduced: the generator's `(\w+)\b,` full-match also skips enumerators followed
 * by a comment, so `BPF_PROG_TYPE_SYSCALL, /* … */` (bpf.h#L1094) never becomes `BPFProgTypeSyscall`
 * and that spelling is rejected by systemd (and every later program type is shifted down one bit —
 * an upstream bug, see the batch QUESTIONS.md).
 *
 * Covers: Service.BPFDelegatePrograms, Socket.BPFDelegatePrograms, Mount.BPFDelegatePrograms, Swap.BPFDelegatePrograms.
 *
 * The empty assignment (mask 0, i.e. nothing delegated) never reaches the grammar.
 */
class ConfigParseBpfDelegateProgramsOptionValue : SimpleGrammarOptionValues(
    "config_parse_bpf_delegate_programs",
    bpfDelegateList(
        FlexibleLiteralChoiceTerminal(
            "BPFProgTypeUnspec", "BPFProgTypeSocketFilter", "BPFProgTypeKprobe", "BPFProgTypeSchedCls", "BPFProgTypeSchedAct",
            "BPFProgTypeTracepoint", "BPFProgTypeXdp", "BPFProgTypePerfEvent", "BPFProgTypeCgroupSkb", "BPFProgTypeCgroupSock",
            "BPFProgTypeLwtIn", "BPFProgTypeLwtOut", "BPFProgTypeLwtXmit", "BPFProgTypeSockOps", "BPFProgTypeSkSkb",
            "BPFProgTypeCgroupDevice", "BPFProgTypeSkMsg", "BPFProgTypeRawTracepoint", "BPFProgTypeCgroupSockAddr", "BPFProgTypeLwtSeg6local",
            "BPFProgTypeLircMode2", "BPFProgTypeSkReuseport", "BPFProgTypeFlowDissector", "BPFProgTypeCgroupSysctl", "BPFProgTypeRawTracepointWritable",
            "BPFProgTypeCgroupSockopt", "BPFProgTypeTracing", "BPFProgTypeStructOps", "BPFProgTypeExt", "BPFProgTypeLsm",
            "BPFProgTypeSkLookup", "BPFProgTypeNetfilter",
        )
    )
)
