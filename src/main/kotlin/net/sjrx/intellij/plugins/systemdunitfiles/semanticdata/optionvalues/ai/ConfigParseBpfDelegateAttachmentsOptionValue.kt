package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.ai

import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.SimpleGrammarOptionValues
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.FlexibleLiteralChoiceTerminal
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar.bpfDelegateList

/*
 * BPFDelegateAttachments= ([Service]/[Socket]/[Mount]/[Swap]).
 *
 * man    https://www.freedesktop.org/software/systemd/man/latest/systemd.exec.html#BPFDelegateAttachments=
 * parser https://github.com/systemd/systemd/blob/0f0682b773/src/core/load-fragment.c#L181   config_parse_bpf_delegate_attachments (0), via DEFINE_CONFIG_PARSE_PTR
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/namespace.h#L287   bpf_delegate_attachments_from_string
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/namespace.c#L4221-L4253   bpf_delegate_from_string ("any", or a "," list; unknown names warned and ignored)
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/namespace.c#L4197   DEFINE_STRING_TABLE_LOOKUP(bpf_delegate_attach_type) — exact streq
 * leaf   https://github.com/systemd/systemd/blob/0f0682b773/src/core/generate-bpf-delegate-configs.py   builds bpf_delegate_attach_type_table from enum bpf_attach_type in src/include/uapi/linux/bpf.h
 * tests  https://github.com/systemd/systemd/blob/0f0682b773/test/units/TEST-07-PID1.private-bpf.sh#L52   BPFDelegateAttachments=BPFFlowDissector,BPFCgroupSysctl,BPFNetfilter
 * linux  https://man7.org/linux/man-pages/man2/bpf.2.html
 */

/**
 * Validator for `BPFDelegateAttachments=` (the bpffs `delegate_attachs=` mount option; only effective with `PrivateBPF=yes`).
 *
 * Either `any` (the whole value) or a comma-separated list of the names below — see [bpfDelegateList]
 * for the splitting rules (commas only, no spaces; empty fields are skipped). The names are what
 * src/core/generate-bpf-delegate-configs.py makes of each `BPF_…` enumerator of `enum bpf_attach_type` in the
 * uapi bpf.h systemd bundles at 0f0682b773 (`BPF_FOO_BAR` → `BPFFooBar`; aliases written as
 * `X = Y,` are skipped by its regex and therefore not accepted). Regenerate with
 *
 *     python3 src/core/generate-bpf-delegate-configs.py code src/include/uapi/linux/bpf.h
 *
 * An unknown name is logged ("Unknown BPF delegate option, ignoring") and dropped, so it is flagged.
 * The list tracks the bundled kernel header, so a newer systemd may know names this one does not.
 *
 * Covers: Service.BPFDelegateAttachments, Socket.BPFDelegateAttachments, Mount.BPFDelegateAttachments, Swap.BPFDelegateAttachments.
 *
 * The empty assignment (mask 0, i.e. nothing delegated) never reaches the grammar.
 */
class ConfigParseBpfDelegateAttachmentsOptionValue : SimpleGrammarOptionValues(
    "config_parse_bpf_delegate_attachments",
    bpfDelegateList(
        FlexibleLiteralChoiceTerminal(
            "BPFCgroupInetIngress", "BPFCgroupInetEgress", "BPFCgroupInetSockCreate", "BPFCgroupSockOps", "BPFSkSkbStreamParser",
            "BPFSkSkbStreamVerdict", "BPFCgroupDevice", "BPFSkMsgVerdict", "BPFCgroupInet4Bind", "BPFCgroupInet6Bind",
            "BPFCgroupInet4Connect", "BPFCgroupInet6Connect", "BPFCgroupInet4PostBind", "BPFCgroupInet6PostBind", "BPFCgroupUdp4Sendmsg",
            "BPFCgroupUdp6Sendmsg", "BPFLircMode2", "BPFFlowDissector", "BPFCgroupSysctl", "BPFCgroupUdp4Recvmsg",
            "BPFCgroupUdp6Recvmsg", "BPFCgroupGetsockopt", "BPFCgroupSetsockopt", "BPFTraceRawTp", "BPFTraceFentry",
            "BPFTraceFexit", "BPFModifyReturn", "BPFLsmMac", "BPFTraceIter", "BPFCgroupInet4Getpeername",
            "BPFCgroupInet6Getpeername", "BPFCgroupInet4Getsockname", "BPFCgroupInet6Getsockname", "BPFXdpDevmap", "BPFCgroupInetSockRelease",
            "BPFXdpCpumap", "BPFSkLookup", "BPFXdp", "BPFSkSkbVerdict", "BPFSkReuseportSelect",
            "BPFSkReuseportSelectOrMigrate", "BPFPerfEvent", "BPFTraceKprobeMulti", "BPFLsmCgroup", "BPFStructOps",
            "BPFNetfilter", "BPFTcxIngress", "BPFTcxEgress", "BPFTraceUprobeMulti", "BPFCgroupUnixConnect",
            "BPFCgroupUnixSendmsg", "BPFCgroupUnixRecvmsg", "BPFCgroupUnixGetpeername", "BPFCgroupUnixGetsockname", "BPFNetkitPrimary",
            "BPFNetkitPeer", "BPFTraceKprobeSession", "BPFTraceUprobeSession", "BPFTraceFsession", "BPFTraceFentryMulti",
            "BPFTraceFexitMulti", "BPFTraceFsessionMulti",
        )
    )
)
